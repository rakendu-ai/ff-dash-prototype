import SwiftUI

/// Placeholder dash layout. Final screens follow the wireframes.
struct DashView: View {
    @EnvironmentObject private var simulator: RideSimulator

    var body: some View {
        let telemetry = simulator.telemetry
        VStack(spacing: 20) {
            HStack {
                Image(systemName: telemetry.isLocked ? "lock.fill" : "lock.open.fill")
                Spacer()
                Text(telemetry.rideMode.rawValue)
                    .font(.headline)
            }

            Text("\(telemetry.speedKmh)")
                .font(.system(size: 120, weight: .bold, design: .rounded))
                .monospacedDigit()
            Text("km/h")
                .foregroundStyle(.secondary)

            HStack(spacing: 24) {
                stat("Battery", "\(telemetry.batteryPercent)%")
                stat("Range", "\(telemetry.rangeKm) km")
                stat("Trip", String(format: "%.1f km", telemetry.tripKm))
            }

            ForEach(telemetry.warnings, id: \.self) { warning in
                Label(warning, systemImage: "exclamationmark.triangle.fill")
                    .foregroundStyle(.orange)
            }

            Spacer()
        }
        .padding()
        .navigationTitle("Dash")
        .navigationBarTitleDisplayMode(.inline)
        .onAppear {
            if simulator.scenario == nil {
                simulator.start(.cityRide)
            }
        }
        .onDisappear {
            simulator.stop()
        }
    }

    private func stat(_ title: String, _ value: String) -> some View {
        VStack(spacing: 4) {
            Text(value)
                .font(.title3.bold())
            Text(title)
                .font(.caption)
                .foregroundStyle(.secondary)
        }
    }
}
