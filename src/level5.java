import java.awt.*;
import java.util.ArrayList;

public class level5 extends level{

    public level5(Image background){
        super(background);
        super.platforms = new ArrayList<>();
        super.traps = new ArrayList<>();
    }

    @Override
    public void load(player player1, player player2, Image platformImage, Image spikeImage, Image sawImage, Image pitImage, Image knifeImage, Image portalImage) {

    }
}
