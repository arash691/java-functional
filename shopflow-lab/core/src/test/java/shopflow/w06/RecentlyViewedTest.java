package shopflow.w06;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("w06")
@DisplayName("EX-06-02 Queue/Deque: recently viewed")
class RecentlyViewedTest {

    @Test
    void mostRecentFirst() {
        RecentlyViewed rv = new RecentlyViewed(5);
        rv.view("a");
        rv.view("b");
        rv.view("c");
        assertThat(rv.items()).containsExactly("c", "b", "a");
    }

    @Test
    void viewingAgainMovesToFrontWithoutDuplicates() {
        RecentlyViewed rv = new RecentlyViewed(5);
        rv.view("a");
        rv.view("b");
        rv.view("a");
        assertThat(rv.items()).containsExactly("a", "b");
    }

    @Test
    void dropsTheOldestBeyondCapacity() {
        RecentlyViewed rv = new RecentlyViewed(3);
        for (String s : new String[] {"a", "b", "c", "d"}) {
            rv.view(s);
        }
        assertThat(rv.items()).containsExactly("d", "c", "b");
    }

    @Test
    void returnedListIsACopy() {
        RecentlyViewed rv = new RecentlyViewed(3);
        rv.view("a");
        rv.items().clear();
        assertThat(rv.items()).containsExactly("a");
    }
}
