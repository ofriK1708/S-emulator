package uiweb.utils;

import javafx.animation.FadeTransition;
import javafx.beans.property.BooleanProperty;
import javafx.scene.control.TableRow;
import javafx.util.Duration;

public class AnimatedTableRow<T> extends TableRow<T> {
    private final BooleanProperty animationsEnabledProperty;
    private final boolean onlyAnimateOnce;
    private final int millisSpeedFactor;
    private boolean wasAnimatedBefore = false;
    private int numberOfAnimations = 0;

    public AnimatedTableRow(BooleanProperty animationsEnabledProperty, int millisSpeedFactor, boolean onlyAnimateOnce) {
        this.animationsEnabledProperty = animationsEnabledProperty;
        this.millisSpeedFactor = millisSpeedFactor;
        this.onlyAnimateOnce = onlyAnimateOnce;
    }

    @Override
    protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        if (!onlyAnimateOnce || numberOfAnimations == 0) {
            checkForFadingInAnimation(item, empty);
            if (item != null && !empty) {
                numberOfAnimations++;
            }
        }
    }

    private void checkForFadingInAnimation(T item, boolean empty) {
        if (!animationsEnabledProperty.get()) {
            return;
        }
        if (item != null && !empty) {
            if (!wasAnimatedBefore) {
                setOpacity(0);
                FadeTransition ft = new FadeTransition(Duration.millis(1000), this);
                ft.setFromValue(0);
                ft.setToValue(1);
                ft.setDelay(Duration.millis(millisSpeedFactor * getIndex()));
                ft.play();
                wasAnimatedBefore = true;
            }
        } else {
            setOpacity(1);
            wasAnimatedBefore = false;
        }
    }

    public void resetAnimationState() {
        numberOfAnimations = 0;
    }
}