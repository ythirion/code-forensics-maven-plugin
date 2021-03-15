require('code-forensics').configure(
  {
    repository: {
      rootPath: "{repositoryPath}",
      excludePaths: [
        'target'
      ]
    }
  }
);