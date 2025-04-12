package com.nukie.app.test;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.nukie.app.data.repository.SocialRepository;

/**
 * Test Activity for running tests and displaying results in the UI.
 * 
 * This activity provides a user interface for running tests and viewing results.
 */
public class TestActivity extends AppCompatActivity {
    private static final String TAG = "TestActivity";
    
    private NukieTestExecutor testExecutor;
    private TestResultsCallback callback;
    
    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.nukie.app.R.layout.activity_test);
        
        // Initialize test executor
        SocialRepository repository = ((com.nukie.app.NukieApplication) getApplication()).getSocialRepository();
        testExecutor = new NukieTestExecutor(this, repository);
        
        // Set up UI elements
        setupUI();
    }
    
    private void setupUI() {
        // Find UI elements
        android.widget.Button runAllTestsButton = findViewById(com.nukie.app.R.id.run_all_tests_button);
        android.widget.Button runFunctionalTestsButton = findViewById(com.nukie.app.R.id.run_functional_tests_button);
        android.widget.Button runUITestsButton = findViewById(com.nukie.app.R.id.run_ui_tests_button);
        android.widget.Button generateReportsButton = findViewById(com.nukie.app.R.id.generate_reports_button);
        
        // Set click listeners
        runAllTestsButton.setOnClickListener(v -> runAllTests());
        runFunctionalTestsButton.setOnClickListener(v -> runFunctionalTests());
        runUITestsButton.setOnClickListener(v -> runUITests());
        generateReportsButton.setOnClickListener(v -> generateReports());
    }
    
    private void runAllTests() {
        showToast("Running all tests...");
        
        // Run tests in background thread
        new Thread(() -> {
            final String report = testExecutor.executeAllTestsAndGenerateReport();
            
            // Update UI on main thread
            new Handler(Looper.getMainLooper()).post(() -> {
                showToast("All tests completed");
                displayResults(report);
                
                if (callback != null) {
                    callback.onTestsCompleted(report);
                }
            });
        }).start();
    }
    
    private void runFunctionalTests() {
        showToast("Running functional tests...");
        
        // Run tests in background thread
        new Thread(() -> {
            final NukieTestRunner.TestResults results = testExecutor.executeTestSuite("functional");
            
            // Update UI on main thread
            new Handler(Looper.getMainLooper()).post(() -> {
                showToast("Functional tests completed");
                displayResults(results.toString());
                
                if (callback != null) {
                    callback.onFunctionalTestsCompleted(results);
                }
            });
        }).start();
    }
    
    private void runUITests() {
        showToast("Running UI tests...");
        
        // Run tests in background thread
        new Thread(() -> {
            final NukieTestRunner.TestResults results = testExecutor.executeTestSuite("ui");
            
            // Update UI on main thread
            new Handler(Looper.getMainLooper()).post(() -> {
                showToast("UI tests completed");
                displayResults(results.toString());
                
                if (callback != null) {
                    callback.onUITestsCompleted(results);
                }
            });
        }).start();
    }
    
    private void generateReports() {
        showToast("Generating reports...");
        
        // Generate reports in background thread
        new Thread(() -> {
            final String performanceReport = testExecutor.generatePerformanceReport();
            final String compatibilityReport = testExecutor.generateCompatibilityReport();
            
            // Update UI on main thread
            new Handler(Looper.getMainLooper()).post(() -> {
                showToast("Reports generated");
                
                // Save reports to files
                saveReportToFile("performance_report.md", performanceReport);
                saveReportToFile("compatibility_report.md", compatibilityReport);
                
                // Display combined report
                displayResults("Reports generated and saved to files:\n" +
                        "- performance_report.md\n" +
                        "- compatibility_report.md");
                
                if (callback != null) {
                    callback.onReportsGenerated(performanceReport, compatibilityReport);
                }
            });
        }).start();
    }
    
    private void saveReportToFile(String filename, String content) {
        try {
            java.io.FileOutputStream fos = openFileOutput(filename, Context.MODE_PRIVATE);
            fos.write(content.getBytes());
            fos.close();
            Log.d(TAG, "Report saved to " + getFilesDir() + "/" + filename);
        } catch (Exception e) {
            Log.e(TAG, "Error saving report to file", e);
        }
    }
    
    private void displayResults(String results) {
        // Display results in TextView
        android.widget.TextView resultsTextView = findViewById(com.nukie.app.R.id.test_results_text_view);
        resultsTextView.setText(results);
        
        // Scroll to top
        android.widget.ScrollView scrollView = findViewById(com.nukie.app.R.id.results_scroll_view);
        scrollView.smoothScrollTo(0, 0);
    }
    
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    
    public void setTestResultsCallback(TestResultsCallback callback) {
        this.callback = callback;
    }
    
    /**
     * Callback interface for test results.
     */
    public interface TestResultsCallback {
        void onTestsCompleted(String report);
        void onFunctionalTestsCompleted(NukieTestRunner.TestResults results);
        void onUITestsCompleted(NukieTestRunner.TestResults results);
        void onReportsGenerated(String performanceReport, String compatibilityReport);
    }
}
