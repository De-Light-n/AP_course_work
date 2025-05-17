package proj.UI.Dialog;

import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.DialogFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;
import proj.Models.Derivative;

import javax.swing.*;

import static org.assertj.core.api.Assertions.assertThat;

public class AddObligationToDerivativeDialogTest extends AssertJSwingJUnitTestCase {
    private DialogFixture window;
    private AddObligationToDerivativeDialog dialog;
    private Derivative testDerivative;

    @Override
    protected void onSetUp() {
        testDerivative = new Derivative("Test Derivative");
        dialog = GuiActionRunner.execute(() -> new AddObligationToDerivativeDialog(new JFrame(), testDerivative));
        window = new DialogFixture(robot(), dialog);
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
    public void testInitialState() {
        window.comboBox().requireSelection("HealthInsurance");
        window.textBox("riskLevelField").requireText("");
        window.textBox("amountField").requireText("");
        window.textBox("durationField").requireText("");
        window.button("saveButton").requireEnabled();
        window.button("cancelButton").requireEnabled();
    }

    @Test
    public void testValidationEmptyFields() {
        window.button("saveButton").click();
        assertThat(dialog.isVisible()).isTrue();
        assertThat(dialog.isSaved()).isFalse();
        assertThat(dialog.getObligation()).isNull();
    }

    @Test
    public void testCancelOperation() {
        window.textBox("riskLevelField").enterText("0.3");
        window.textBox("amountField").enterText("50000");
        window.textBox("durationField").enterText("12");
        window.button("cancelButton").click();
        assertThat(dialog.isSaved()).isFalse();
        assertThat(dialog.getObligation()).isNull();
    }

    @Test
    public void testAddHealthInsurance() {
        window.comboBox().requireVisible();
        window.comboBox().selectItem("HealthInsurance");
        window.textBox("riskLevelField").enterText("0.3");
        window.textBox("amountField").enterText("50000");
        window.textBox("durationField").enterText("12");
        window.textBox("notesField").enterText("Test note");

        // Додайте setName для відповідних полів у detailsPanel!
        window.textBox("ageField").enterText("35");
        window.checkBox("preexistingConditionsCheck").check();
        window.textBox("coverageLimitField").enterText("200000");
        window.checkBox("hospitalizationCheck").check();
        window.checkBox("dentalCareCheck").check();

        window.button("saveButton").click();
        assertThat(dialog.isSaved()).isTrue();
        assertThat(dialog.getObligation()).isNotNull();
    }

    @Test
    public void testAddLifeInsurance() {
        window.comboBox().requireVisible();
        window.comboBox().selectItem("LifeInsurance");
        window.textBox("riskLevelField").enterText("0.4");
        window.textBox("amountField").enterText("100000");
        window.textBox("durationField").enterText("24");
        window.textBox("notesField").enterText("Life note");

        window.textBox("beneficiaryField").enterText("John Doe");
        window.checkBox("criticalIllnessCheck").check();
        window.checkBox("accidentalDeathCheck").check();

        window.button("saveButton").click();
        assertThat(dialog.isSaved()).isTrue();
        assertThat(dialog.getObligation()).isNotNull();
    }

    @Test
    public void testAddPropertyInsurance() {
        window.comboBox().requireVisible();
        window.comboBox().selectItem("PropertyInsurance");
        window.textBox("riskLevelField").enterText("0.5");
        window.textBox("amountField").enterText("500000");
        window.textBox("durationField").enterText("36");
        window.textBox("notesField").enterText("Property note");

        window.textBox("locationField").enterText("Kyiv");
        window.textBox("propertyValueField").enterText("1000000");
        window.checkBox("highRiskAreaCheck").check();
        window.comboBox("propertyTypeCombo").selectItem("APARTMENT");
        window.checkBox("naturalDisastersCheck").check();

        window.button("saveButton").click();
        assertThat(dialog.isSaved()).isTrue();
        assertThat(dialog.getObligation()).isNotNull();
    }

    @Test
    public void testDynamicPanelChange() {
        window.comboBox().selectItem("HealthInsurance");
        assertThat(window.panel("detailsPanel").target().getComponentCount()).isGreaterThan(0);

        window.comboBox().selectItem("LifeInsurance");
        assertThat(window.panel("detailsPanel").target().getComponentCount()).isGreaterThan(0);

        window.comboBox().selectItem("PropertyInsurance");
        assertThat(window.panel("detailsPanel").target().getComponentCount()).isGreaterThan(0);
    }
}