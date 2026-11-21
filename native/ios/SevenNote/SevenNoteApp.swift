import SwiftUI
import AVFoundation

@main
struct SevenNoteApp: App {
    init() {
        try? AVAudioSession.sharedInstance().setCategory(.playback, mode: .default, options: [.mixWithOthers])
    }

    var body: some Scene {
        WindowGroup {
            GameView()
                .background(Color(red: 8/255, green: 13/255, blue: 29/255))
                .ignoresSafeArea(.container, edges: .bottom)
        }
    }
}
