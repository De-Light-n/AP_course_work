package proj;

import org.junit.jupiter.api.Test;

import javax.swing.*;

import static org.junit.jupiter.api.Assertions.*;

public class InsuranceAppTest {

    @Test
    public void testInsuranceAppConstructor() {
        SwingUtilities.invokeLater(() -> {
            InsuranceApp app = new InsuranceApp();
            assertNotNull(app);
            assertEquals("Система страхування", app.getTitle());
            app.dispose();
        });
    }

    @Test
    public void testMainMethodRuns() {
        // Просто перевіряємо, що main не кидає винятків
        assertDoesNotThrow(() -> {
            InsuranceApp.main(new String[] {});
        });
    }
}
