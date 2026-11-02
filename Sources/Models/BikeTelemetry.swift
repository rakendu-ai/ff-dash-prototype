import Foundation

/// Field names follow the FF App's SSE vehicle-status fields (battery %, lock, GPS/LTE,
/// tyre pressure, trip odometer, ride mode, lat/long). These are prototype names, not wire names.
struct BikeTelemetry: Equatable {
    var speedKmh: Int = 0
    var batteryPercent: Int = 80
    var rangeKm: Int = 160
    var rideMode: RideMode = .eco
    var isLocked: Bool = true
    var gpsFix: Bool = true
    var lteBars: Int = 4
    var tyrePressureFrontBar: Double = 2.2
    var tyrePressureRearBar: Double = 2.4
    var tripKm: Double = 0
    var warnings: [String] = []
}

/// Placeholder list until the lead confirms the real ride modes per model.
enum RideMode: String, CaseIterable, Identifiable {
    case eco = "Eco"
    case city = "City"
    case tour = "Tour"
    case sport = "Sport"

    var id: String { rawValue }
}
