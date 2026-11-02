import Combine
import Foundation

/// A bike product in the catalogue. Only some products offer the mobile cluster option.
struct BikeProduct: Hashable, Identifiable {
    let id: String
    let name: String
    /// Set per product by the product team. Placeholder until confirmed.
    let supportsMobileCluster: Bool
}

enum Catalogue {
    static let flyingFleaC6 = BikeProduct(id: "ff-c6", name: "Flying Flea C6", supportsMobileCluster: false)
    /// Placeholder for the next products. Replace with real names and flags.
    static let placeholderProductB = BikeProduct(id: "product-b", name: "Product B (name TBC)", supportsMobileCluster: true)

    static let all = [flyingFleaC6, placeholderProductB]
}

/// Availability needs both checks to pass:
/// 1. the user's bike product offers the mobile cluster, and
/// 2. the user's Firestore flag for the module is "Yes" (set after purchase).
struct PhoneDashEntitlement: Equatable {
    var product: BikeProduct
    var firestoreFlagEnabled: Bool

    var isOffered: Bool { product.supportsMobileCluster }
    var isAvailable: Bool { isOffered && firestoreFlagEnabled }
}

@MainActor
final class PrototypeState: ObservableObject {
    @Published var entitlement = PhoneDashEntitlement(product: Catalogue.flyingFleaC6, firestoreFlagEnabled: false)
    @Published var hasOnboarded = false

    /// Mocked purchase. Stands in for the backend setting the Firestore flag to "Yes". No payment is taken.
    func purchase() {
        entitlement.firestoreFlagEnabled = true
    }

    func reset() {
        entitlement = PhoneDashEntitlement(product: Catalogue.flyingFleaC6, firestoreFlagEnabled: false)
        hasOnboarded = false
    }
}
