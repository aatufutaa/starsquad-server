package git.aatufutaa.game.game.games.tutorial;

import git.aatufutaa.game.game.DynamicGameData;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TutorialDynamicGameData extends DynamicGameData {

    private final TutorialStage tutorialStage;

    private final boolean showSpinner;
    private final int spinnerX;
    private final int spinnerY;

    @Override
    public void write(ByteBuf buf) {
        buf.writeByte(this.tutorialStage.ordinal());

        buf.writeBoolean(this.showSpinner);
        if (this.showSpinner) {
            buf.writeByte(this.spinnerX);
            buf.writeByte(this.spinnerY);
        }
    }

}
