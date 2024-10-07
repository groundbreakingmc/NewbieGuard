package groundbreaking.newbieguard.utils.time;

import org.bukkit.Statistic;
import org.bukkit.entity.Player;

public final class OnlineCounter implements ITimeCounter {

    @Override
    public long count(Player player) {
        return player.getStatistic(Statistic.PLAY_ONE_MINUTE) / 20;
    }
}
