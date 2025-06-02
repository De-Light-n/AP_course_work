package proj.UI.Tab;

import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;
import proj.Models.Derivative;
import proj.Models.insurance.*;
import proj.Service.*;

import javax.swing.*;
import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;

public class DerivativeDetailsTabTest extends AssertJSwingJUnitTestCase {

    private DerivativeDetailsTab detailsTab;
    private JTabbedPane tabbedPane;
    private FrameFixture window;
    private Derivative testDerivative;
    private DerivativeService derivativeService = DerivativeService.getInstance();

    @Override
    protected void onSetUp() {
        
        // Create test data
        testDerivative = new Derivative("Test Derivative");

        // Add test obligations
        HealthInsurance health = new HealthInsurance(0.5, 1000.0, 12, 30, true, 5000, true, true);
        PropertyInsurance property = new PropertyInsurance(0.3, 2000.0, 24, "Kyiv", 1_000_000, true, "APARTMENT", true);
        testDerivative.addObligation(health);
        testDerivative.addObligation(property);

        // Create tabbed pane and details tab through EDT
        tabbedPane = GuiActionRunner.execute(() -> new JTabbedPane());
        detailsTab = GuiActionRunner.execute(() -> new DerivativeDetailsTab(tabbedPane, testDerivative));

        // Create and show frame
        JFrame frame = GuiActionRunner.execute(() -> {
            JFrame f = new JFrame();
            f.setContentPane(detailsTab);
            f.pack();
            f.setVisible(true);
            return f;
        });
        window = new FrameFixture(robot(), frame);
        window.show();
    }

    @Override
    protected void onTearDown() {
        if (window != null) {
            if (window.target().isShowing()) {
                window.close();
            }
            window.cleanUp();
        }
    }

    @Test
    public void testInitialization() {
        // Verify main components are visible
        window.button("addButton").requireVisible();
        window.button("deleteButton").requireVisible();
        window.button("removeDerivativeButton").requireVisible();
        window.comboBox("sortOptions").requireVisible();
        window.textBox("searchField").requireVisible();
        window.textBox("minCalcValueField").requireVisible();
        window.textBox("maxCalcValueField").requireVisible();
        window.table("obligationsTable").requireVisible();

        // Verify summary panel
        window.label("totalLabel").requireVisible();
        window.label("countLabel").requireVisible();
        window.label("avgRiskLabel").requireVisible();
    }

    @Test
    public void testFilterObligationsBySearchText() {
        // Search for health insurance
        window.textBox("searchField").setText("HEALTH");

        // Verify only one row is visible in the table
        JTable table = window.table("obligationsTable").target();
        assertEquals(1, table.getRowCount());
        assertEquals("HEALTH", table.getValueAt(0, 1)); // Type column
    }

    @Test
    public void testFilterObligationsByCalculatedValue() {
        // Set value range that matches only HEALTH insurance
        window.textBox("minCalcValueField").setText("1500");
        window.textBox("maxCalcValueField").setText("4000");

        // Verify only one row is visible in the table
        JTable table = window.table("obligationsTable").target();
        assertEquals(1, table.getRowCount());
        assertEquals("HEALTH", table.getValueAt(0, 1)); // Type column
    }

    @Test
    public void testSortObligationsByRiskAsc() {
        window.comboBox("sortOptions").selectItem("Рівень ризику (зростання)");

        // Verify rows are sorted by risk level ascending
        JTable table = window.table("obligationsTable").target();
        assertEquals(0.3, table.getValueAt(0, 2)); // Property insurance first (lower risk)
        assertEquals(0.5, table.getValueAt(1, 2)); // Health insurance second
    }

    @Test
    public void testSortObligationsByRiskDesc() {
        window.comboBox("sortOptions").selectItem("Рівень ризику (спадання)");

        // Verify rows are sorted by risk level descending
        JTable table = window.table("obligationsTable").target();
        assertEquals(0.5, table.getValueAt(0, 2)); // Health insurance first (higher risk)
        assertEquals(0.3, table.getValueAt(1, 2)); // Property insurance second
    }

    @Test
    public void testAddObligationButton() {
        window.button("addButton").click();

        // Шукаємо діалог за setName
        JDialog dialog = findDialogByName("addObligationDialog");
        assertNotNull(dialog, "Add obligation dialog should be visible");

        dialog.dispose();
    }

    @Test
    public void testDeleteObligationButton() {
        // Select first row in table
        window.table("obligationsTable").selectRows(0);

        // Click delete button
        window.button("deleteButton").click();

        // Verify confirmation dialog is shown
        JOptionPane optionPane = findOptionPane();
        assertNotNull(optionPane, "Confirmation dialog should be shown");

        // Close dialog by clicking "No"
        window.optionPane().noButton().click();
    }

    @Test
    public void testRemoveDerivativeButton() {
        window.button("removeDerivativeButton").click();

        // Verify confirmation dialog is shown
        JOptionPane optionPane = findOptionPane();
        assertNotNull(optionPane, "Confirmation dialog should be shown");

        // Close dialog by clicking "No"
        window.optionPane().noButton().click();
    }

    @Test
    public void testDoubleClickOnObligation() {
        window.table("obligationsTable").selectRows(0);
        window.table("obligationsTable").cell(org.assertj.swing.data.TableCell.row(0).column(0)).doubleClick();

        // Шукаємо діалог за setName
        JDialog dialog = findDialogByName("obligationDetailsDialog");
        assertNotNull(dialog, "Obligation details dialog should be visible");

        dialog.dispose();
    }

    @Test
    public void testSummaryPanelContent() {
        // Verify summary panel shows correct values
        JLabel totalLabel = window.label("totalLabel").target();
        assertTrue(totalLabel.getText().contains(String.valueOf(testDerivative.getTotalValue())));

        JLabel countLabel = window.label("countLabel").target();
        assertTrue(countLabel.getText().contains(String.valueOf(testDerivative.getObligations().size())));

        JLabel avgRiskLabel = window.label("avgRiskLabel").target();
        assertTrue(avgRiskLabel.getText().contains(String.valueOf(derivativeService.calculateAverageRisk(testDerivative))));
    }

    // Helper method to find dialog by name
    private JDialog findDialogByName(String name) {
        for (Window w : Window.getWindows()) {
            if (w instanceof JDialog && w.isVisible() && name.equals(w.getName())) {
                return (JDialog) w;
            }
        }
        return null;
    }

    // Helper method to find option pane
    private JOptionPane findOptionPane() {
        for (Window w : Window.getWindows()) {
            if (w instanceof JDialog) {
                JDialog d = (JDialog) w;
                if (d.isVisible() && d.getContentPane().getComponentCount() > 0
                        && d.getContentPane().getComponent(0) instanceof JOptionPane) {
                    return (JOptionPane) d.getContentPane().getComponent(0);
                }
            }
        }
        return null;
    }
}