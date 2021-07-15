which.median = function(x) {
  if (length(x) %% 2 != 0) {
    which(x == median(x))
  } else if (length(x) %% 2 == 0) {
    a = sort(x)[c(length(x)/2, length(x)/2+1)]
    c(which(x == a[1]), which(x == a[2]))
  }
}

alg_patterns <- c('MIO-GOMEA-BDeu-16', 'MODELMOSA-AC-BDeu-10', 'MOSA-GOMEA-BDeu-16')
features_data <- data.frame()
for(alg_pattern in alg_patterns) {
  
  # Determine run with median AUC
  alg_data <- data.frame()
  for(i in seq(1, 20)) {
    file_name <- paste(alg_pattern, '-', i, '.csv', sep= "")
    file_path <- fs::path('features', file_name)
    data <- readr::read_csv(file_path)
    auc <- pracma::trapz(data$time, data$coveredTargets)
    auc_data <- data.frame(i, auc)
    alg_data <- rbind(alg_data, auc_data)
  }
  
  alg_median <- alg_data[which.median(alg_data$auc), ]
  alg_median_index <- alg_median[which.max(alg_median$auc), ]$i
  
  file_name <- paste(alg_pattern, '-', alg_median_index, '.csv', sep= "")
  file_path <- fs::path('features', file_name)
  alg_median_data <- readr::read_csv(file_path)
  
  alg_median_data$system <- 'Features-Service'
  alg_median_data$index <- alg_median_index
  
  if (alg_pattern == 'MIO-GOMEA-BDeu-16') {
    alg_median_data$algorithm <- 'MIO'
  } else if (alg_pattern == 'MODELMOSA-AC-BDeu-10') {
    alg_median_data$algorithm <- 'LT-MOSA'
  } else {
    alg_median_data$algorithm <- 'MOSA'
  }
  
  features_data <- rbind(features_data, alg_median_data)
}

features_data2 <- dplyr::filter(features_data, mod(generation, 10) == 0)

ggplot2::ggplot(features_data2, aes(x=time, y=coveredTargets)) +
#  geom_point(size=2, aes(shape=algorithm)) +
  geom_line(size=0.75, aes(linetype=algorithm, color=algorithm)) +
  theme_bw() +
  theme(legend.justification=c(1,0), legend.position = c(0.98, 0.02), legend.background = element_rect(fill="white", size=0.1, linetype="solid", colour ="black")) +
  labs(x = "Time (seconds)", y = "Number of Covered Branches", linetype = "Search Algorithms", color = "Search Algorithms")
  #  expand_limits(x = 0, y = 0)
  #  scale_x_continuous(expand = c(0, 0), limits = c(-1,16), breaks = seq(0, 15, len = 16)) +
  #  scale_y_continuous(expand = c(0, 0), limits = c(0,120))
  
ggsave("features.pdf", width = 12, height = 8, units = "cm")
