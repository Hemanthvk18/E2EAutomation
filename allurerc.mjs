import { defineConfig } from 'allure';

export default defineConfig({
  name: "E2E Cucumber Automation Report",
  output: './allure-report',

  plugins: {
    awesome: {
      options: {
        reportName: 'E2E Cucumber Automation Report',
        singleFile: true,
        theme: 'dark',
        reportLanguage: 'en',
        publish: true,
      },
    },
  },

  log: {
    options: {}
  },

  groupBy: "none",
  filter: () => true
});