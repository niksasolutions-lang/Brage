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

// 1. Unauthenticated rejection
test("Unauthenticated user: cannot read or write user profile", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice",
      email: "alice@test.com",
      createdAt: new Date(),
    })
  );
});

// 2. Cross-user isolation
test("Authenticated user: cannot read another user's profile or bookings", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(BOB_UID).set({
      userId: BOB_UID,
      displayName: "Bob",
      email: "bob@test.com",
      createdAt: new Date(),
    });
    await context.firestore().collection("users").doc(BOB_UID).collection("bookings").doc("booking_bob_1").set({
      id: "booking_bob_1",
      userId: BOB_UID,
      serviceId: "clean_deep",
      serviceTitle: "Deep Clean",
      categoryTitle: "Cleaning",
      providerName: "Elena",
      providerInitials: "ER",
      status: "CONFIRMED",
      scheduledDate: "Tomorrow",
      scheduledTimeSlot: "10:00 AM",
      address: "123 Main St",
      contactPhone: "555-1234",
      basePrice: 129,
      addOnsDescription: "None",
      addOnsCost: 0,
      isExpress: false,
      expressFee: 0,
      totalPrice: 129,
      specialInstructions: "Ring bell",
      userRating: 0,
      userReview: "",
      createdAt: new Date(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).get());
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("bookings").doc("booking_bob_1").get());
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("bookings").get());
});

// 3. User can create, read, and update own profile
test("Authenticated user: can create and update own profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const profileRef = aliceDb.collection("users").doc(ALICE_UID);

  await assertSucceeds(
    profileRef.set({
      userId: ALICE_UID,
      displayName: "Alice Tester",
      email: "alice@test.com",
      photoUrl: "https://example.com/avatar.jpg",
      phone: "+15551234567",
      createdAt: new Date(),
    })
  );

  await assertSucceeds(profileRef.get());

  await assertSucceeds(
    profileRef.update({
      displayName: "Alice Updated",
      phone: "+15559876543",
    })
  );
});

// 4. User can create, read, and update own bookings
test("Authenticated user: can create, read, and manage own bookings", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bookingRef = aliceDb.collection("users").doc(ALICE_UID).collection("bookings").doc("booking_123");

  await assertSucceeds(
    bookingRef.set({
      id: "booking_123",
      userId: ALICE_UID,
      serviceId: "repair_plumb",
      serviceTitle: "Plumbing Leak Fix",
      categoryTitle: "Repairs & Handyman",
      providerName: "David Chen",
      providerInitials: "DC",
      status: "CONFIRMED",
      scheduledDate: "Oct 10, 2026",
      scheduledTimeSlot: "2:00 PM - 4:00 PM",
      address: "742 Evergreen Terrace",
      contactPhone: "+15553829912",
      basePrice: 85,
      addOnsDescription: "High-pressure jetting",
      addOnsCost: 40,
      isExpress: true,
      expressFee: 15,
      totalPrice: 140,
      specialInstructions: "Gate code is 1234",
      userRating: 0,
      userReview: "",
      createdAt: new Date(),
    })
  );

  await assertSucceeds(bookingRef.get());
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("bookings").get());

  // Update status and user rating
  await assertSucceeds(
    bookingRef.update({
      status: "COMPLETED",
      userRating: 5,
      userReview: "Phenomenal work by David!",
    })
  );
});

// 5. User can create and delete favorites
test("Authenticated user: can manage own favorites", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const favRef = aliceDb.collection("users").doc(ALICE_UID).collection("favorites").doc("clean_deep");

  await assertSucceeds(
    favRef.set({
      serviceId: "clean_deep",
      createdAt: new Date(),
    })
  );

  await assertSucceeds(favRef.get());
  await assertSucceeds(favRef.delete());
});
