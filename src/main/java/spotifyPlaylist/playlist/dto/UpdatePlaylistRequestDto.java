package spotifyPlaylist.playlist.dto;

public class UpdatePlaylistRequestDto {
    private String playlistName;
    private int backgroundIdx;

    // Getters and setters

    public String getPlaylistName() {
        return playlistName;
    }

    public void setPlaylistName(String playlistName) {
        this.playlistName = playlistName;
    }

    public int getBackgroundIdx() {
        return backgroundIdx;
    }

    public void setBackgroundIdx(int backgroundIdx) {
        this.backgroundIdx = backgroundIdx;
    }
}
