package proj.UI.Dialog;

import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.DialogFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;
import proj.Models.insurance.*;
import proj.Models.insurance.InsuranceObligation.ObligationStatus;
import proj.Repositories.InsuranceObligationRepository;

import javax.swing.*;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ObligationDetailsDialogTest extends AssertJSwingJUnitTestCase {

    private DialogFixture window;
    private ObligationDetailsDialog dialog;
    private InsuranceObligationRepository mockRepository;
    private InsuranceObligation testObligation;

    @Override
    protected void onSetUp() {
        // Створіть об'єкт згідно з конструктором
        testObligation = new HealthInsurance(
                0.5, 1000.0, 12,
                30, true, 5000, true, true);
        testObligation.setStatus(ObligationStatus.ACTIVE);

        mockRepository = mock(InsuranceObligationRepository.class);

        dialog = GuiActionRunner.execute(() -> new ObligationDetailsDialog(new JFrame(), testObligation) {
            @Override
            protected InsuranceObligationRepository getRepository() {
                return mockRepository;
            }
        });

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
    public void shouldDisplayAllCommonFieldsCorrectly() {
        window.comboBox("statusCombo").requireSelection(ObligationStatus.ACTIVE.name());
        ;
        window.textBox("amountField").requireText("1000.0");
        window.textBox("riskLevelField").requireText("0.5");
        window.textBox("durationField").requireText("12");
        window.label("calculatedValueLabel").requireText("Розрахована вартість:");
    }

    @Test
    public void shouldDisplayHealthInsuranceSpecificFields() {
        window.label("ageLabel").requireText("Вік:");
        window.label("ageValueLabel").requireText("30");
        window.label("preexistingLabel").requireText("Хронічні хвороби:");
        window.label("preexistingValueLabel").requireText("Так");
        window.label("coverageLimitLabel").requireText("Ліміт покриття:");
        window.label("coverageLimitValueLabel").requireText("5000");
        window.label("hospitalizationLabel").requireText("Госпіталізація:");
        window.label("hospitalizationValueLabel").requireText("Так");
        window.label("dentalCareLabel").requireText("Стоматологія:");
        window.label("dentalCareValueLabel").requireText("Так");
    }

    @Test
    public void shouldUpdateDataWhenFieldsAreModified() {
        window.comboBox("statusCombo").selectItem(ObligationStatus.EXPIRED.name());
        window.textBox("policyNumberField").setText("POL123_UPDATED");
        window.textBox("amountField").setText("1500.0");
        window.textBox("riskLevelField").setText("0.7");
        window.textBox("durationField").setText("24");
        window.textBox("notesField").setText("Test notes_UPDATED");
        assertThat(dialog.isDataChanged()).isTrue();
    }

    @Test
    public void shouldSaveChangesWhenSaveButtonClicked() throws SQLException {
        window.textBox("amountField").setText("1500.0");
        window.textBox("riskLevelField").setText("0.7");
        window.textBox("durationField").setText("24");
        window.textBox("notesField").setText("Test notes_UPDATED");
        window.button("saveButton").click();
        assertThat(dialog.isSaved()).isTrue();
    }

    @Test
    public void shouldCloseWithoutSavingWhenCancelButtonClicked() throws SQLException {
        window.textBox("policyNumberField").setText("POL123_SHOULD_NOT_SAVE");
        window.button("cancelButton").click();
        assertThat(dialog.isVisible()).isFalse();
        assertThat(dialog.isSaved()).isFalse();
        verify(mockRepository, never()).save(any());
    }

    @Test
    public void shouldShowErrorWhenInvalidDataEntered() {
        window.textBox("amountField").setText("invalid");
        window.button("saveButton").click();

        // Закрити JOptionPane натисканням Enter
        robot().pressAndReleaseKeys(java.awt.event.KeyEvent.VK_ENTER);

        assertThat(dialog.isVisible()).isTrue();
        assertThat(dialog.isSaved()).isFalse();
    }

    @Test
    public void shouldDisplayLifeInsuranceSpecificFields() {
        InsuranceObligation lifeObligation = new LifeInsurance(
                0.3, 2000.0, 24,
                "John Doe", true, true);

        dialog.dispose();
        dialog = GuiActionRunner.execute(() -> new ObligationDetailsDialog(new JFrame(), lifeObligation));
        window = new DialogFixture(robot(), dialog);
        window.show();

        window.label("beneficiaryLabel").requireText("Бенефіціар:");
        window.label("beneficiaryValueLabel").requireText("John Doe");
        window.label("criticalIllnessLabel").requireText("Критичні хвороби:");
        window.label("criticalIllnessValueLabel").requireText("Так");
        window.label("accidentalDeathLabel").requireText("Нещасні випадки:");
        window.label("accidentalDeathValueLabel").requireText("Так");
    }

    @Test
    public void shouldDisplayPropertyInsuranceSpecificFields() {
        InsuranceObligation propertyObligation = new PropertyInsurance(
                0.2, 3000.0, 36,
                "Київ", 1_000_000, true, "APARTMENT", true);

        dialog.dispose();
        dialog = GuiActionRunner.execute(() -> new ObligationDetailsDialog(new JFrame(), propertyObligation));
        window = new DialogFixture(robot(), dialog);
        window.show();

        window.label("propertyLocationLabel").requireText("Місцезнаходження:");
        window.label("propertyLocationValueLabel").requireText("Київ");
        window.label("propertyValueLabel").requireText("Вартість:");
        window.label("propertyValueValueLabel").requireText("1000000.0");
        window.label("highRiskAreaLabel").requireText("Зона ризику:");
        window.label("highRiskAreaValueLabel").requireText("Так");
        window.label("propertyTypeLabel").requireText("Тип нерухомості:");
        window.label("propertyTypeValueLabel").requireText("APARTMENT");
        window.label("naturalDisastersLabel").requireText("Стихійні лиха:");
        window.label("naturalDisastersValueLabel").requireText("Так");
    }
}