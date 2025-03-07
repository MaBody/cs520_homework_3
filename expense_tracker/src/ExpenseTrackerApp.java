import javax.swing.JOptionPane;
import controller.ExpenseTrackerController;
import model.ExpenseTrackerModel;
import view.ExpenseTrackerView;
import model.Filter.AmountFilter;
import model.Filter.CategoryFilter;

public class ExpenseTrackerApp {

  /**
   * @param args
   */
  public static void main(String[] args) {

    // Create MVC components
    ExpenseTrackerModel model = new ExpenseTrackerModel();
    ExpenseTrackerView view = new ExpenseTrackerView();
    ExpenseTrackerController controller = new ExpenseTrackerController(model, view);
    // controller.addTransaction(1, "food");
    // controller.addTransaction(2, "travel");
    // controller.addTransaction(3, "bills");
    // controller.addTransaction(4, "bills");

    // Initialize view
    view.setVisible(true);

    // Handle add transaction button clicks
    view.getAddTransactionBtn().addActionListener(e -> {
      // Get transaction data from view
      double amount = view.getAmountField();
      String category = view.getCategoryField();

      // Call controller to add transaction
      boolean added = controller.addTransaction(amount, category);

      if (!added) {
        JOptionPane.showMessageDialog(view, "Invalid amount or category entered");
        view.toFront();
      }
    });

    // Add action listener to the "Apply Category Filter" button
    view.addApplyCategoryFilterListener(e -> {
      try {
        String categoryFilterInput = view.getCategoryFilterInput();
        CategoryFilter categoryFilter = new CategoryFilter(categoryFilterInput);
        if (categoryFilterInput != null) {
          // controller.applyCategoryFilter(categoryFilterInput);
          controller.setFilter(categoryFilter);
          controller.applyFilter();
        }
      } catch (IllegalArgumentException exception) {
        JOptionPane.showMessageDialog(view, exception.getMessage());
        view.toFront();
      }
    });

    // Add action listener to the "Apply Amount Filter" button
    view.addApplyAmountFilterListener(e -> {
      try {
        double amountFilterInput = view.getAmountFilterInput();
        AmountFilter amountFilter = new AmountFilter(amountFilterInput);
        if (amountFilterInput != 0.0) {
          controller.setFilter(amountFilter);
          controller.applyFilter();
        }
      } catch (IllegalArgumentException exception) {
        JOptionPane.showMessageDialog(view, exception.getMessage());
        view.toFront();
      }
    });
    view.addUndoAllowedListener(e -> {
      int[] selectedRows = view.getTransactionsTable().getSelectedRows();
      if (selectedRows.length != 1 || selectedRows[0] >= model.getTransactions().size()) {
        view.setUndoBtn(false);
      } else {
        view.setUndoBtn(true);
      }
    });

    view.addUndoListener(e -> {
      int[] selectedRows = view.getTransactionsTable().getSelectedRows();
      try {
        controller.removeTransactionByIndex(selectedRows);
      } catch (IllegalArgumentException exception) {
        JOptionPane.showMessageDialog(view, exception.getMessage());
        view.toFront();
      }
    });
  }
}
