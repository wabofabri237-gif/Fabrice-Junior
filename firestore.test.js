const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

// --- SECURITY BOUNDS TESTS ---

test("Unauthenticated user: cannot read user profiles", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
});

test("Authenticated user: cannot read another user's profile directly", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(BOB_UID).set({
      userId: BOB_UID,
      email: "bob@example.com",
      displayName: "Bob",
      role: "CLIENT",
      createdAt: new Date(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).get());
});

test("Authenticated user: can create own profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      email: "alice@example.com",
      displayName: "Alice",
      role: "CLIENT",
      phone: "+237690000000",
      city: "Douala",
      createdAt: new Date(),
    })
  );
});

test("Public user: can read products catalog", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("products").doc("prod_1").set({
      id: "prod_1",
      sellerId: BOB_UID,
      title: "Café Moka de l'Ouest",
      price: 2500,
      currency: "XAF",
      category: "Épicerie",
      inStock: true,
      createdAt: new Date(),
    });
  });

  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertSucceeds(unauthDb.collection("products").doc("prod_1").get());
});

test("Authenticated user: can create an order for themselves", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("orders").doc("order_1").set({
      id: "order_1",
      userId: ALICE_UID,
      sellerId: BOB_UID,
      totalAmount: 15000,
      paymentMethod: "CASH_ON_DELIVERY",
      deliveryAddress: "Akwa, rue des Palmiers",
      deliveryCity: "Douala",
      contactPhone: "+237670112233",
      deliveryOtp: "4829",
      status: "PENDING",
      createdAt: new Date(),
    })
  );
});

test("Authenticated user: cannot read another user's order", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("orders").doc("order_bob").set({
      id: "order_bob",
      userId: BOB_UID,
      sellerId: "other_seller",
      totalAmount: 5000,
      paymentMethod: "CASH_ON_DELIVERY",
      deliveryAddress: "Bastos",
      deliveryCity: "Yaoundé",
      contactPhone: "+237699112233",
      status: "PENDING",
      createdAt: new Date(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("orders").doc("order_bob").get());
});

test("Public user: can read bank accounts", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("bank_accounts").doc("afriland_1").set({
      id: "afriland_1",
      bankName: "Afriland First Bank",
      accountName: "NboaShop SARL",
      iban: "CM2110005000123456789012345",
      rib: "10005 00012 34567890123 45",
      swift: "AFRICMCX",
      active: true,
    });
  });

  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertSucceeds(unauthDb.collection("bank_accounts").doc("afriland_1").get());
});
