
// package test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;
import java.text.ParseException;

import org.junit.Before;
import org.junit.Test;

import controller.ExpenseTrackerController;
import model.ExpenseTrackerModel;
import model.Transaction;
import view.ExpenseTrackerView;

public class TestExample {

    private ExpenseTrackerModel model;
    private ExpenseTrackerView view;
    private ExpenseTrackerController controller;

    @Before
    public void setup() {
        model = new ExpenseTrackerModel();
        view = new ExpenseTrackerView();
        controller = new ExpenseTrackerController(model, view);
    }

    public double getTotalCost() {
        double totalCost = 0.0;
        List<Transaction> allTransactions = model.getTransactions(); // Using the model's getTransactions method
        for (Transaction transaction : allTransactions) {
            totalCost += transaction.getAmount();
        }
        return totalCost;
    }

    public double getTotalCostFromView() {
        int totalSumIdx = view.getTransactionsTable().getRowCount();
        int colIdx = view.getTransactionsTable().getColumnCount();
        Object obj = view.getTransactionsTable().getValueAt(totalSumIdx - 1, colIdx - 1);
        double totalCost = (double) obj;
        return totalCost;
    }

    public Transaction getTransactionFromView(int index) {
        double amount = (double) view.getTransactionsTable().getValueAt(index, 1);
        String category = (String) view.getTransactionsTable().getValueAt(index, 2);
        Transaction t = new Transaction(amount, category);
        return t;
    }

    public void checkTransaction(double amount, String category, Transaction transaction) {
        assertEquals(amount, transaction.getAmount(), 0.01);
        assertEquals(category, transaction.getCategory());
        String transactionDateString = transaction.getTimestamp();
        Date transactionDate = null;
        try {
            transactionDate = Transaction.dateFormatter.parse(transactionDateString);
        } catch (ParseException pe) {
            pe.printStackTrace();
            transactionDate = null;
        }
        Date nowDate = new Date();
        assertNotNull(transactionDate);
        assertNotNull(nowDate);
        // They may differ by 60 ms
        assertTrue(nowDate.getTime() - transactionDate.getTime() < 60000);
    }

    @Test
    public void testAddTransaction() {
        // Pre-condition: List of transactions is empty
        assertEquals(0, model.getTransactions().size());

        // Perform the action: Add a transaction
        double amount = 50.0;
        String category = "food";
        assertTrue(controller.addTransaction(amount, category));

        // Post-condition: List of transactions contains only
        // the added transaction
        assertEquals(1, model.getTransactions().size());

        // Check the contents of the list
        Transaction firstTransaction = model.getTransactions().get(0);
        checkTransaction(amount, category, firstTransaction);

        // Check the total amount
        assertEquals(amount, getTotalCost(), 0.01);
    }

    @Test
    public void testRemoveTransaction() {
        // Pre-condition: List of transactions is empty
        assertEquals(0, model.getTransactions().size());

        // Perform the action: Add and remove a transaction
        double amount = 50.0;
        String category = "food";
        Transaction addedTransaction = new Transaction(amount, category);
        model.addTransaction(addedTransaction);

        // Pre-condition: List of transactions contains only
        // the added transaction
        assertEquals(1, model.getTransactions().size());
        Transaction firstTransaction = model.getTransactions().get(0);
        checkTransaction(amount, category, firstTransaction);

        assertEquals(amount, getTotalCost(), 0.01);

        // Perform the action: Remove the transaction
        model.removeTransaction(addedTransaction);

        // Post-condition: List of transactions is empty
        List<Transaction> transactions = model.getTransactions();
        assertEquals(0, transactions.size());

        // Check the total cost after removing the transaction
        double totalCost = getTotalCost();
        assertEquals(0.00, totalCost, 0.01);
    }

    /**
     * Test case 1.
     */
    @Test
    public void testAddTransactionGUI() {
        // Pre-condition: List of transactions is empty
        assertEquals(0, model.getTransactions().size());

        // Perform the action: Add a transaction
        double amount = 50.0;
        String category = "food";
        assertTrue(controller.addTransaction(amount, category));

        // Post-condition: Model and view contain only one transaction
        assertEquals(1, model.getTransactions().size());
        int tableSize = view.getTransactionsTable().getRowCount();
        // Table contains one additional row for total cost
        assertEquals(tableSize, 2);

        // Post-condition: Added transaction is correct in view and model
        Transaction firstTransactionView = getTransactionFromView(0);
        Transaction firstTransactionModel = model.getTransactions().get(0);
        checkTransaction(amount, category, firstTransactionView);
        checkTransaction(amount, category, firstTransactionModel);

        // Post-condition: Total cost is correct in view and model
        assertEquals(amount, getTotalCost(), 0.01);
        assertEquals(amount, getTotalCostFromView(), 0.01);
    }

    /**
     * Test case 2.
     */
    @Test
    public void testAddInvalidTransactionGUI() {
        // Pre-condition: List of transactions is empty
        assertEquals(0, model.getTransactions().size());

        // Add a transaction
        double amount = 50.0;
        String category = "food";
        assertTrue(controller.addTransaction(amount, category));

        double amountInvalid = 0.0;
        String categoryInvalid = "foood";

        // Perform the action: Attempt to add invalid inputs
        assertFalse(controller.addTransaction(amountInvalid, category));
        assertFalse(controller.addTransaction(amount, categoryInvalid));

        // Post-condition: Model and view contain only one transaction
        assertEquals(1, model.getTransactions().size());
        int tableSize = view.getTransactionsTable().getRowCount();
        // Table contains one additional row for total cost
        assertEquals(tableSize, 2);

        // Post-condition: Existing transaction is unaffected in view and model
        Transaction firstTransactionView = getTransactionFromView(0);
        Transaction firstTransactionModel = model.getTransactions().get(0);
        checkTransaction(amount, category, firstTransactionView);
        checkTransaction(amount, category, firstTransactionModel);

        // Post-condition: Total cost is unaffected in view and model
        assertEquals(amount, getTotalCost(), 0.01);
        assertEquals(amount, getTotalCostFromView(), 0.01);
    }

    /**
     * Test case 5.
     */
    @Test
    public void testUndoDisallowed() {
        // Undo transaction when transaction list is empty

        // Pre-condition: List of transactions is empty
        assertEquals(0, model.getTransactions().size());

        // Perform the action: try to remove a non-existing transaction
        // Index zero in this scenario corresponds to the row for the total sum
        int[] invalidIndex = new int[] { 0 };
        try {
            controller.removeTransactionByIndex(invalidIndex);
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }

        // Post-condition: Model and view contain no transactions
        assertEquals(0, model.getTransactions().size());
        int tableSize = view.getTransactionsTable().getRowCount();
        // Table does not contain total cost field yet
        assertEquals(tableSize, 0);
    }

    /**
     * Test case 6.
     */
    @Test
    public void testUndoAllowed() {
        // Pre-condition: List of transactions is empty
        assertEquals(0, model.getTransactions().size());

        // Add transaction
        double amount = 50.0;
        String category = "food";
        controller.addTransaction(amount, category);

        // Pre-condition: Transaction in view is updated correctly
        assertEquals(1, model.getTransactions().size());
        Transaction firstTransaction = getTransactionFromView(0);
        checkTransaction(amount, category, firstTransaction);
        // Pre-condition: Total cost in view is updated correctly
        assertEquals(amount, getTotalCost(), 0.01);

        // Perform the action: remove the transaction
        int[] validIndex = new int[] { 0 };
        controller.removeTransactionByIndex(validIndex);

        // Post-condition: Model and view contain no transactions
        assertEquals(0, model.getTransactions().size());
        int tableSize = view.getTransactionsTable().getRowCount();
        // Table contains one additional row for total cost
        assertEquals(tableSize, 1);

        // Post-condition: Total cost is now zero in view and model
        assertEquals(0, getTotalCost(), 0.01);
        assertEquals(0, getTotalCostFromView(), 0.01);
    }

}
