package proj.UI.Dialog;

import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.DialogFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;
import proj.Models.Derivative;

import javax.swing.*;

import static org.assertj.core.api.Assertions.assertThat;

public class AddEditDerivativeDialogTest extends AssertJSwingJUnitTestCase {
    private DialogFixture window;
    private AddEditDerivativeDialog dialog;

    @Override
    protected void onSetUp() {
        dialog = GuiActionRunner.execute(() -> {
            JFrame testFrame = new JFrame();
            testFrame.setVisible(true);
            return new AddEditDerivativeDialog(testFrame, null);
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
    public void testInitialState() {
        // Перевірка початкового стану
        window.textBox().requireText("");
        window.button("saveButton").requireEnabled();
        window.button("cancelButton").requireEnabled();
    }

    @Test
    public void testAddNewDerivative() {
        // Введення даних та збереження
        window.textBox().enterText("Новий дериватив");
        window.button("saveButton").click();

        // Перевірка результатів
        assertThat(dialog.isSaved()).isTrue();
        assertThat(dialog.getDerivative()).isNotNull();
        assertThat(dialog.getDerivative().getName()).isEqualTo("Новий дериватив");
    }

    @Test
    public void testEditExistingDerivative() {
        window.close(); // Закриваємо попередній діалог

        Derivative testDerivative = new Derivative("Тестовий дериватив");
        dialog = GuiActionRunner.execute(() -> new AddEditDerivativeDialog(new JFrame(), testDerivative));
        window = new DialogFixture(robot(), dialog);
        window.show();

        window.textBox().requireText("Тестовий дериватив");
        window.button("saveButton").click();

        assertThat(dialog.isSaved()).isTrue();
        assertThat(dialog.getDerivative()).isEqualTo(testDerivative);
    }

    @Test
    public void testCancelOperation() {
        window.textBox().enterText("Скасований дериватив");
        window.button("cancelButton").click();

        assertThat(dialog.isSaved()).isFalse();
        assertThat(dialog.getDerivative()).isNull();
    }

    @Test
    public void testValidationEmptyName() {
        // Введення порожнього імені
        window.textBox().setText("");
        window.button("saveButton").click();

        // Діалог не повинен закриватися, isSaved має бути false
        assertThat(dialog.isSaved()).isFalse();
        assertThat(dialog.getDerivative()).isNull();
        // Можна додати перевірку, що діалог все ще видимий
        assertThat(window.target().isShowing()).isTrue();
    }
}