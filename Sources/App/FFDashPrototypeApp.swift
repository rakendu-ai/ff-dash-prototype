import SwiftUI

@main
struct FFDashPrototypeApp: App {
    @StateObject private var state = PrototypeState()
    @StateObject private var simulator = RideSimulator()

    var body: some Scene {
        WindowGroup {
            GarageHomeView()
                .environmentObject(state)
                .environmentObject(simulator)
        }
    }
}
