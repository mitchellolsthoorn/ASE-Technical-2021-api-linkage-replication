system_data <- data.frame()

systems <- fs::dir_ls(type='directory')
for(system in systems) {
  if (system == "scs") {
    alg_patterns <- c('MIO-GOMEA-BDeu-10', 'MODELMOSA-AC-BDeu-10', 'MOSA-GOMEA-BDeu-10')
  } else {
    alg_patterns <- c('MIO-GOMEA-BDeu-16', 'MODELMOSA-AC-BDeu-10', 'MOSA-GOMEA-BDeu-16')
  }
  
  highest_cov <- 1
  alg_data <- data.frame()
  
  for(alg_pattern in alg_patterns) {
    # Determine run with median AUC
    run_data <- data.frame()
    for(i in seq(1, 20)) {
      file_name <- paste(alg_pattern, '-', i, '.csv', sep= "")
      file_path <- fs::path(system, file_name)
      data <- readr::read_csv(file_path)
      auc <- pracma::trapz(data$time, data$coveredTargets)
      
      highest_cov <- max(highest_cov, max(data$coveredTargets))
      
      auc_data <- data.frame(i, auc)
      run_data <- rbind(run_data, auc_data)
    }
    
    run_median_auc <- median(run_data$auc)
    
    if (alg_pattern == 'MIO-GOMEA-BDeu-16' || alg_pattern == 'MIO-GOMEA-BDeu-10') {
      algorithm <- 'MIO'
    } else if (alg_pattern == 'MODELMOSA-AC-BDeu-10') {
      algorithm <- 'LT-MOSA'
    } else {
      algorithm <- 'MOSA'
    }
    
    run_median_data <- data.frame(system, algorithm, run_median_auc)
    
    alg_data <- rbind(alg_data, run_median_data)
  }
  
  alg_data <- mutate(alg_data, normalized_auc=run_median_auc/(highest_cov*1800))
  alg_data$highest_auc <- (highest_cov*1800)
  system_data <- rbind(system_data, alg_data)
}

system_data_spread <- tidyr::spread(select(system_data, system, algorithm, normalized_auc), algorithm, normalized_auc)
