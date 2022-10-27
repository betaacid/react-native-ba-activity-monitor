import Foundation
import CoreMotion
import React

typealias Activity = NSDictionary
typealias OnActivitiesCallback = (NSArray) -> Void

@objc(BaActivityMonitor)
class BaActivityMonitor: RCTEventEmitter {
    
    private var isListeningForActivities = false
    private var activityManager = CMMotionActivityManager()

    @objc(askPermissionIOS:reject:)
    func askPermissionIOS(_ resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) -> Void {
        switch CMMotionActivityManager.authorizationStatus() {
            case .authorized: return resolve("granted")
            case .notDetermined:
            activityManager.queryActivityStarting(from: Date.init(timeIntervalSinceNow: -10000), to: Date(), to: .main) { _, error in
                guard error == nil else {
                    return resolve("denied")
                }
                resolve("granted")
            }
            case .denied: return resolve("denied")
            case .restricted: return resolve("blocked")
            default: return reject("invalid_coremotion_authstatus", "Invalid core motion authorization status.", nil)
        }
    }
    
    @objc(isStarted:reject:)
    func isStarted(_ resolve:RCTPromiseResolveBlock, reject:RCTPromiseRejectBlock) {
        resolve(isListeningForActivities)
    }
    
    @objc(start:reject:)
    func start(_ resolve:RCTPromiseResolveBlock, reject:RCTPromiseRejectBlock) {
        guard !isListeningForActivities else {
            return resolve(true)
        }
        
        let authorizationStatus = CMMotionActivityManager.authorizationStatus()
        guard authorizationStatus == .authorized || authorizationStatus == .notDetermined else {
            return reject("invalid_permission_status", "Permission needed.", nil)
        }
        
        activityManager.startActivityUpdates(to: .main) { activity in
            guard let activity = activity else { return }
            self.sendEvent(withName: "activities", body: NSArray(array: [activity.jsObject]))
        }
        isListeningForActivities = true
        resolve(true)
    }
    
    @objc(stop)
    func stop() {
        guard isListeningForActivities else { return }
        isListeningForActivities = false
        activityManager.stopActivityUpdates()
    }
    
    @objc(sendMockActivities:)
    func sendMockActivities(_ activities: NSArray) {
        self.sendEvent(withName: "activities", body: activities)
    }

    override func supportedEvents() -> [String]! {
        return ["activities"]
    }
    
    override func invalidate() {
        self.stop()
    }
    
}
