document.addEventListener('DOMContentLoaded', () => {

  if (typeof evolduo === 'undefined') {
    return
  }

  // data

  const userData = {
    labels: evolduo.stats.users.dates,
    datasets: [{
      label: 'User Registrations',
      backgroundColor: 'rgb(255, 99, 132)',
      borderColor: 'rgb(255, 99, 132)',
      data: evolduo.stats.users.counts
    }]
  };

  const ratingData = {
    labels: evolduo.stats.ratings.dates,
    datasets: [{
      label: 'Ratings',
      backgroundColor: 'rgb(61, 90, 254)',
      borderColor: 'rgb(61, 90, 254)',
      data: evolduo.stats.ratings.counts
    }]
  };

  const evolutionData = {
    labels: evolduo.stats.evolutions.dates,
    datasets: [{
      label: 'Evolutions',
      backgroundColor: 'rgb(255, 145, 0)',
      borderColor: 'rgb(255, 145, 0)',
      data: evolduo.stats.users.counts
    }]
  };

  const chromosomeData = {
    labels: evolduo.stats.chromosomes.dates,
    datasets: [{
      label: 'Tracks',
      backgroundColor: 'rgb(56, 142, 60)',
      borderColor: 'rgb(56, 142, 60)',
      data: evolduo.stats.chromosomes.counts
    }]
  };

  // configs

  const userConfig = {
    type: 'line',
    data: userData,
    options: {
      maintainAspectRatio: false
    }
  };

  const ratingConfig = {
    type: 'line',
    data: ratingData,
    options: {
      maintainAspectRatio: false
    }
  };

  const evolutionConfig = {
    type: 'line',
    data: evolutionData,
    options: {
      maintainAspectRatio: false
    }
  };

  const chromosomeConfig = {
    type: 'line',
    data: chromosomeData,
    options: {
      maintainAspectRatio: false
    }
  };

  // charts

  const userChart = new Chart(
      document.getElementById('user-stats'),
      userConfig
    );

  const ratingChart = new Chart(
      document.getElementById('rating-stats'),
      ratingConfig
    );

  const evolutionChart = new Chart(
      document.getElementById('evolution-stats'),
      evolutionConfig
    );

  const chromosomeChart = new Chart(
      document.getElementById('chromosome-stats'),
      chromosomeConfig
    );
});
