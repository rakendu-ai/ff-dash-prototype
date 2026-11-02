import SwiftUI

struct PaywallView: View {
    @EnvironmentObject private var state: PrototypeState
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        VStack(spacing: 24) {
            Image(systemName: "iphone")
                .font(.system(size: 64))
            Text("Phone as a Dash")
                .font(.title.bold())
            Text("Turn your phone into your bike's dash. One-time fee, no subscription.")
                .multilineTextAlignment(.center)
            Text("Price: [TBC by product team]")
                .foregroundStyle(.secondary)
            Button("Buy (mock, no payment)") {
                state.purchase()
                dismiss()
            }
            .buttonStyle(.borderedProminent)
            Spacer()
        }
        .padding()
        .navigationTitle("Unlock")
        .navigationBarTitleDisplayMode(.inline)
    }
}
