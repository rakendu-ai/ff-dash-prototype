# FF Phone as a Dash: iOS prototype

Stakeholder prototype of the Phone as a Dash module. It runs on mock data only, with no BLE, no Firestore, no payments and no real keys.

**Status:** source written but not yet compiled or run. It has to be built on a Mac.

## Entitlement model (to confirm with the lead developer)

The module is available when both checks pass:

1. The user's bike product offers the mobile cluster (per-product flag, placeholder values in `Sources/App/PrototypeState.swift`).
2. The user's Firestore flag for the module is `"Yes"`. The purchase sets it, and the prototype's Paywall mocks that step.

Only some products offer the option. Flying Flea C6 does not, so its Garage shows no tile.

## Layout

```
project.yml                    XcodeGen spec (generates the .xcodeproj)
Sources/App/                   app entry, entitlement + product state
Sources/Models/                telemetry and ride-mode types
Sources/Services/              ride simulator (scripted scenarios)
Sources/Features/Garage/       home screen and the module tile
Sources/Features/Paywall/      one-time fee screen (mocked purchase)
Sources/Features/Onboarding/   placeholder setup pages
Sources/Features/Dash/         dash layout
Sources/Features/Demo/         hidden demo controls (3-second long press on Garage)
```

## Build on a Mac

Requires Xcode 15 or later and [XcodeGen](https://github.com/yonaskolb/XcodeGen).

```bash
brew install xcodegen
cd FFDashPrototype
xcodegen generate
open FFDashPrototype.xcodeproj
```

Select an iPhone simulator and press Run. This project does not include the Bosch navigation frameworks, so the Simulator build works here, unlike the FF App.

For a device or IPA, set your Apple Developer team under Signing and use TestFlight (internal testers) or Ad Hoc.

## Placeholders to replace

- Price, copy and product names
- Ride modes and their order
- Fonts and colours (system fonts for now; FF design tokens later)
- Onboarding pages (replace with the wireframes)
- Dash layout (replace with the wireframes)
- Firestore flag and product flag reads (currently `PrototypeState`)

## Differences from the FF App

- Uses `ObservableObject` / `@Published` with async-free code. The FF App uses Combine throughout, so align when the module is ported.
- No `Screens/MainNavigation/`, `renavigation` or Bosch SDK code is used.
