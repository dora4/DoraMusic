package site.doramusic.app.event

import site.doramusic.app.model.IPFSMusic

class DeleteTaskEvent(music: IPFSMusic) {
    var music: IPFSMusic = music
}
