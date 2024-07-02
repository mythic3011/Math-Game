async function getTopScores() {
    try {
        const response = await fetch('/top_scores');
        const topScores = await response.json();
        let html = '<table class="min-w-full"><thead><tr class="bg-gray-200"><th class="px-4 py-2">Rank</th><th class="px-4 py-2">Player</th><th class="px-4 py-2">Score</th><th class="px-4 py-2">Time</th></tr></thead><tbody>';
        topScores.forEach((score, index) => {
            html += `<tr class="${index % 2 === 0 ? 'bg-gray-100' : 'bg-white'}">
                <td class="px-4 py-2">${index + 1}</td>
                <td class="px-4 py-2">${score.player_name}</td>
                <td class="px-4 py-2">${score.correct_count}/10</td>
                <td class="px-4 py-2">${score.duration}s</td>
            </tr>`;
        });
        html += '</tbody></table>';
        document.getElementById('topScores').innerHTML = html;
    } catch (error) {
        console.error('Error fetching top scores:', error);
    }
}

async function getDailyLeaderboard() {
    const date = document.getElementById('dateInput').value;
    if (!date) {
        alert('Please select a date');
        return;
    }
    try {
        const response = await fetch(`/daily_leaderboard/${date}`);
        const leaderboard = await response.json();
        let html = '<table class="min-w-full"><thead><tr class="bg-gray-200"><th class="px-4 py-2">Rank</th><th class="px-4 py-2">Player</th><th class="px-4 py-2">Score</th><th class="px-4 py-2">Time</th></tr></thead><tbody>';
        leaderboard.forEach((score, index) => {
            html += `<tr class="${index % 2 === 0 ? 'bg-gray-100' : 'bg-white'}">
                <td class="px-4 py-2">${index + 1}</td>
                <td class="px-4 py-2">${score.player_name}</td>
                <td class="px-4 py-2">${score.correct_count}/10</td>
                <td class="px-4 py-2">${score.duration}s</td>
            </tr>`;
        });
        html += '</tbody></table>';
        document.getElementById('dailyLeaderboard').innerHTML = html;
    } catch (error) {
        console.error('Error fetching daily leaderboard:', error);
    }
}

async function getPlayerStats() {
    const playerName = document.getElementById('playerNameInput').value;
    if (!playerName) {
        alert('Please enter a player name');
        return;
    }
    try {
        const response = await fetch(`/player_stats/${playerName}`);
        const stats = await response.json();
        let html = `
            <p><strong>Player:</strong> ${stats.player_name}</p>
            <p><strong>Total Games:</strong> ${stats.total_games}</p>
            <p><strong>Average Score:</strong> ${stats.avg_score.toFixed(2)}/10</p>
            <p><strong>Average Duration:</strong> ${stats.avg_duration.toFixed(2)}s</p>
            <p><strong>Best Score:</strong> ${stats.best_score}/10</p>
            <p><strong>Best Time:</strong> ${stats.best_time}s</p>
        `;
        document.getElementById('playerStats').innerHTML = html;
    } catch (error) {
        console.error('Error fetching player stats:', error);
        document.getElementById('playerStats').innerHTML = '<p class="text-red-500">Player not found or has no games</p>';
    }
}

async function getScoreDistribution() {
    try {
        const response = await fetch('/game_results');
        const gameResults = await response.json();
        const scoreCounts = Array(11).fill(0);
        gameResults.forEach(result => {
            scoreCounts[result.correct_count]++;
        });

        const ctx = document.getElementById('scoreDistribution').getContext('2d');
        new Chart(ctx, {
            type: 'bar',
            data: {
                labels: Array.from({length: 11}, (_, i) => i),
                datasets: [{
                    label: 'Number of Games',
                    data: scoreCounts,
                    backgroundColor: 'rgba(75, 192, 192, 0.6)',
                    borderColor: 'rgba(75, 192, 192, 1)',
                    borderWidth: 1
                }]
            },
            options: {
                scales: {
                    y: {
                        beginAtZero: true,
                        title: {
                            display: true,
                            text: 'Number of Games'
                        }
                    },
                    x: {
                        title: {
                            display: true,
                            text: 'Score'
                        }
                    }
                },
                plugins: {
                    title: {
                        display: true,
                        text: 'Score Distribution'
                    }
                }
            }
        });
    } catch (error) {
        console.error('Error fetching score distribution:', error);
    }
}

// Load data when the page loads
getTopScores();
getScoreDistribution();
