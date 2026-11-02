import SwiftUI

enum Route: Hashable {
    case paywall
    case onboarding
    case dash
}

/// Garage home. The Phone as a Dash tile only appears for products that offer the mobile cluster.
struct GarageHomeView: View {
    @EnvironmentObject private var state: PrototypeState
    @State private var showDemoPanel = false

    var body: some View {
        NavigationStack {
            List {
                Section("My Bike") {
                    Text(state.entitlement.product.name)
                        .font(.headline)
                    if state.entitlement.isOffered {
                        PhoneDashTile()
                    }
                }
            }
            .navigationTitle("Garage")
            .onLongPressGesture(minimumDuration: 3) {
                showDemoPanel = true
            }
            .navigationDestination(for: Route.self) { route in
                switch route {
                case .paywall: PaywallView()
                case .onboarding: OnboardingView()
                case .dash: DashView()
                }
            }
            .sheet(isPresented: $showDemoPanel) {
                DemoPanelView()
            }
        }
    }
}

private struct PhoneDashTile: View {
    @EnvironmentObject private var state: PrototypeState

    var body: some View {
        if state.entitlement.isAvailable {
            NavigationLink(value: state.hasOnboarded ? Route.dash : Route.onboarding) {
                label
            }
        } else {
            NavigationLink(value: Route.paywall) {
                label
            }
        }
    }

    private var label: some View {
        HStack(spacing: 12) {
            Image(systemName: state.entitlement.isAvailable ? "iphone" : "lock.fill")
                .font(.title2)
            VStack(alignment: .leading, spacing: 2) {
                Text("Phone as a Dash")
                Text(state.entitlement.isAvailable ? "Available" : "Locked · One-time fee")
                    .font(.caption)
                    .foregroundStyle(.secondary)
            }
        }
    }
}
