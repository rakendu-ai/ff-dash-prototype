import SwiftUI

/// Placeholder onboarding. Permissions and pairing are mocked, so nothing is requested from the device.
struct OnboardingView: View {
    @EnvironmentObject private var state: PrototypeState
    @Environment(\.dismiss) private var dismiss
    @State private var page = 0

    private let pages = [
        ("Mount your phone", "Placeholder: mount and orientation guidance."),
        ("Pair with your bike", "Placeholder: Bluetooth pairing, mocked in this prototype."),
        ("Allow permissions", "Placeholder: location, Bluetooth and notifications, mocked here.")
    ]

    var body: some View {
        VStack {
            TabView(selection: $page) {
                ForEach(pages.indices, id: \.self) { index in
                    VStack(spacing: 16) {
                        Text(pages[index].0)
                            .font(.title2.bold())
                        Text(pages[index].1)
                            .multilineTextAlignment(.center)
                    }
                    .padding()
                    .tag(index)
                }
            }
            .tabViewStyle(.page)

            Button(page < pages.count - 1 ? "Next" : "Finish") {
                if page < pages.count - 1 {
                    page += 1
                } else {
                    state.hasOnboarded = true
                    dismiss()
                }
            }
            .buttonStyle(.borderedProminent)
            .padding()
        }
        .navigationTitle("Setup")
        .navigationBarTitleDisplayMode(.inline)
    }
}
