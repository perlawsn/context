package composeBlock;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.dei.perla.lang.query.statement.Refresh;
import org.junit.Test;

public class ComposeRefreshTest {
	
	@Test
	public void selectRefresh(){
		Refresh venti_milli = new Refresh(Duration.of(20, ChronoUnit.MILLIS));
		Refresh un_secondo = new Refresh(Duration.of(1, ChronoUnit.SECONDS));
		Refresh tre_secondi = new Refresh(Duration.of(1, ChronoUnit.SECONDS));
		Refresh cinque_minuti = new Refresh(Duration.of(1, ChronoUnit.MINUTES));
		Refresh dieci_minuti = new Refresh(Duration.of(1, ChronoUnit.MINUTES));
		Refresh un_ora = new Refresh(Duration.of(1, ChronoUnit.HOURS));
		List<Refresh> list = Arrays.asList(new Refresh[]{un_ora, cinque_minuti, venti_milli, un_secondo,
				tre_secondi, dieci_minuti});
		Collections.sort(list, new Comparator<Refresh>(){
	    public int compare(Refresh ra, Refresh rb){
	        Duration da = ra.getDuration();
	        Duration db = rb.getDuration(); 
	        return da.compareTo(db);
	    }
		});
		assertThat(list.get(0).getDuration(), equalTo(Duration.of(20, ChronoUnit.MILLIS)));

		
		
	}

	
}
