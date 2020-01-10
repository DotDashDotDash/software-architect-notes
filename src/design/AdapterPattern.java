package design;

public class AdapterPattern {

    public static void main(String[] args) {
        PlayerAdapter adapter = new AdapterPattern().new PlayerAdapter();
        adapter.play("VcPlayer");
    }

    interface Player {
        void play(String audioType);
    }

    interface AdvancedPlayer {
        void playVc();

        void playMedia();
    }

    class VcPlayer implements AdvancedPlayer {
        @Override
        public void playVc() {
            System.out.println("this is Vc player");
        }

        @Override
        public void playMedia() {

        }
    }

    class MediaPlayer implements AdvancedPlayer {
        @Override
        public void playVc() {

        }

        @Override
        public void playMedia() {
            System.out.println("this is media player");
        }
    }

    class PlayerAdapter implements Player {
        @Override
        public void play(String audioType) {
            AdvancedPlayer player = null;
            if (audioType.equalsIgnoreCase("VcPlayer")) {
                player = new VcPlayer();
                player.playVc();
            } else {
                player = new MediaPlayer();
                player.playMedia();
            }

        }
    }
}
