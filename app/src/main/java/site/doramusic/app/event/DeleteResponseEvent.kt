package site.doramusic.app.event

import site.doramusic.app.model.IPFSMusic

class DeleteResponseEvent(var isDelete: Boolean, music: IPFSMusic) {
    var music: IPFSMusic = music
}
