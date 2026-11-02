import SwiftUI

/// Hidden stakeholder control panel. Opened from the Garage with a 3-second long press.
struct DemoPanelView: View {
    @EnvironmentObject private var state: PrototypeState
    @EnvironmentObject private var simulator: RideSimulator
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            Form {
                Section("Product") {
                    Picker("Bike product", selection: $state.entitlement.product) {
                        ForEach(Catalogue.all, id: \.self) { product in
                            Text(product.name).tag(product)
                        }
                    }
                }

                Section("Entitlement") {
                    Toggle("Firestore flag = Yes", isOn: $state.entitlement.firestoreFlagEnabled)
                    Toggle("Onboarding done", isOn: $state.hasOnboarded)
                }

                Section("Ride scenario") {
                    ForEach(RideScenario.allCases) { scenario in
                        Button(scenario.title) {
                            simulator.start(scenario)
                        }
                    }
                    Button("Stop ride", role: .destructive) {
                        simulator.stop()
                    }
                }

                Section {
                    Button("Reset prototype") {
                        state.reset()
                        simulator.stop()
                    }
                }
            }
            .navigationTitle("Demo controls")
            .toolbar {
                ToolbarItem(placement: .confirmationAction) {
                    Button("Done") { dismiss() }
                }
            }
        }
    }
}
