package shopflow.w05.packaging;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("w05")
@DisplayName("INC-05-2 Gift cubes are charged for the wrong volume")
class PackagingTest {

    @Test
    @DisplayName("GUARD: plain boxes resize correctly")
    void plainBoxVolume() {
        Box resized = Packaging.resized(Boxes.of(1, 2, 3), 4, 5, 6);
        assertThat(resized.volume()).isEqualTo(120);
    }

    @Test
    @DisplayName("GUARD: new boxes and cubes report their own volume")
    void freshVolumes() {
        assertThat(Boxes.of(2, 3, 4).volume()).isEqualTo(24);
        assertThat(Boxes.cube(10).volume()).isEqualTo(1000);
        assertThat(Boxes.cube(10).isCube()).isTrue();
    }

    @Test
    @DisplayName("a cube resized to 10x20x30 has volume 6000")
    void resizedCubeUsesRequestedDimensions() {
        Box resized = Packaging.resized(Boxes.cube(10), 10, 20, 30);
        assertThat(resized.volume()).as("volume of 10x20x30").isEqualTo(6000);
    }

    @Test
    @DisplayName("a cube resized to 10x20x30 is no longer a cube")
    void resizedCubeIsNotACube() {
        Box resized = Packaging.resized(Boxes.cube(10), 10, 20, 30);
        assertThat(resized.isCube()).isFalse();
    }

    @Test
    @DisplayName("resizing does not change the box the caller passed in")
    void originalIsUntouched() {
        Box original = Boxes.of(1, 2, 3);
        Packaging.resized(original, 9, 9, 9);
        assertThat(original.volume()).as("original volume").isEqualTo(6);
    }
}
