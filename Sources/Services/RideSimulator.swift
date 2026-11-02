import Combine
import Foundation

enum RideScenario: String, CaseIterable, Identifiable {
    case cityRide
    case lowBattery

    var id: String { rawValue }

    var title: String {
        switch self {
        case .cityRide: return "City ride"
        case .lowBattery: return "Low battery"
        }
    }

    var startBattery: Int {
        switch self {
        case .cityRide: return 80
        case .lowBattery: return 14
        }
    }
}

/// Plays a scripted ride so the dash moves without a real bike. Mock data only, no BLE.
@MainActor
final class RideSimulator: ObservableObject {
    @Published private(set) var telemetry = BikeTelemetry()
    @Published private(set) var scenario: RideScenario?

    private var timer: AnyCancellable?
    private var tick = 0

    func start(_ scenario: RideScenario) {
        stop()
        self.scenario = scenario
        tick = 0
        telemetry = BikeTelemetry(batteryPercent: scenario.startBattery, isLocked: false)
        timer = Timer.publish(every: 1, on: .main, in: .common)
            .autoconnect()
            .sink { [weak self] _ in self?.step() }
    }

    func stop() {
        timer?.cancel()
        timer = nil
        scenario = nil
    }

    private func step() {
        tick += 1
        // Sine-wave speed profile, roughly 10 to 70 km/h.
        let phase = Double(tick % 60) / 60 * 2 * Double.pi
        telemetry.speedKmh = max(0, Int(40 + 30 * sin(phase)))
        telemetry.tripKm += Double(telemetry.speedKmh) / 3600
        if tick % 10 == 0 && telemetry.batteryPercent > 0 {
            telemetry.batteryPercent -= 1
        }
        telemetry.rangeKm = telemetry.batteryPercent * 2
        telemetry.warnings = telemetry.batteryPercent <= 15 ? ["Low battery: charge soon"] : []
    }
}
