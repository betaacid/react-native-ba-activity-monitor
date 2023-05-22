//
//  CMMotionActivity+Extensions.swift
//  react-native-ba-activity-monitor
//
//  Created by Gabriel Valério on 07/05/22.
//

import CoreMotion

extension CMMotionActivity {
    
    var jsTransitionType: String {
        "enter" // exit not supported on iOS (we can mock it)
    }
    
    var jsType: String {
        if stationary { return "still" }
        if walking { return "walking" }
        if running { return "running" }
        if automotive { return "in-vehicle" }
        if cycling { return "on-bicycle" }
        return "unknown"
    }
    
    var jsObject: NSDictionary {
        NSDictionary(dictionary: [
            "type": self.jsType,
            "confidence": self.confidence.normalizedConfidence
        ])
    }
}

extension CMMotionActivityConfidence {
    
    var normalizedConfidence: Int {
        switch(self) {
        case .high: return 100
        case .medium: return 66
        case .low: return 33
        default: return 0
        }
    }
    
}
