$(document).ready(function () {
    setOption();
    setDeliverySelect();
});

function setOption() {
    const yearSelectElement = $('.yearSelect');
    const monthSelectElement = $('.monthSelect');
    // 실제로는 사용자 정보를 가져와서 여기서 year에 대입해주고 가입날짜를 넣어줘야함
    for (var i = memberYear; i <= nowYear; i++) {
        let option;
        if (i == nowYear) {
            option = '<option value="' + i + '"selected>' + i + '년</option>';
        } else {
            option = '<option value="' + i + '">' + i + '년</option>';
        }
        yearSelectElement.append(option);
    }
    for (var i = memberMonth; i <= nowMonth; i++) {
        let option;
        if (i == nowMonth) {
            option = '<option value="' + i + '" selected>' + i + '월</option>';
        } else {
            option = '<option value="' + i + '">' + i + '월</option>';
        }
        monthSelectElement.append(option);
    }
}

$('.yearSelect').on('change', function () {
    let yearValue = $(this).val();
    let monthSelectElement = $('#monthSelect');
    monthSelectElement.empty();
    if (year != yearValue) {
        for (var i = 1; i <= 12; i++) {
            let option;
            if (i == 12) {
                option = '<option value="' + i + '" selected>' + i + '월</option>';
            } else {
                option = '<option value="' + i + '">' + i + '월</option>';
            }
            monthSelectElement.append(option);
        }
    } else if (year == yearValue) {
        for (var i = 1; i <= month; i++) {
            let option;
            if (i == month) {
                option = '<option value="' + i + '" selected>' + i + '월</option>';
            } else {
                option = '<option value="' + i + '">' + i + '월</option>';
            }
            monthSelectElement.append(option);
        }
    }
});

$('#monthSelect').on('change', function () {
    let yearVal = $('#yearSelect').val();
    let monthVal = $('#monthSelect').val();
    ajaxChartUpdate(yearVal, monthVal);
});

const labels = [nowMonth - 2 + '월', nowMonth - 1 + '월', nowMonth + '월'];
const data = [chartData.twoMonthsAgoTotalFee, chartData.lastMonthTotalFee, chartData.thisMonthTotalFee];
const dataOrders = [chartData.twoMonthsAgoOrderCount, chartData.lastMonthOrderCount, chartData.thisMonthOrderCount];

const chart = new Chart(document.getElementById('responsiveChart').getContext('2d'), {
    type: 'line',
    data: {
        labels: labels,
        datasets: [
            {
                label: nowYear + '년',
                data: dataOrders,
                backgroundColor: 'rgba(255, 107,53 , 1)',
                borderColor: 'rgba(255, 107,53, 1)',
                borderWidth: 1,
            },
        ],
    },
    options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
            title: {
                display: true,
                text: '월별 배송 건수',
            },
            tooltip: {
                callbacks: {
                    label: function (context) {
                        let value = context.raw; // 실제 값
                        return ' ' + value.toLocaleString() + '건';
                    },
                },
            },
        },
        scales: {
            y: {
                beginAtZero: true,
                position: 'left',
                ticks: {
                    callback: function (value) {
                        return value.toLocaleString() + ' 건'; // 숫자 포맷 + "건"
                    },
                },
            },
        },
    },
});
const ctx = document.getElementById('myChart').getContext('2d');

const myChart = new Chart(ctx, {
    type: 'bar',
    data: {
        labels: labels,
        datasets: [
            {
                label: nowYear + '년',
                data: data,
                backgroundColor: 'rgba(255, 107,53 , 1)',
                borderColor: 'rgba(255, 107,53, 1)',
                borderWidth: 1,
            },
        ],
    },
    options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
            title: {
                display: true,
                text: '월별 매출 분석',
            },
            tooltip: {
                callbacks: {
                    label: function (context) {
                        let value = context.raw; // 실제 값
                        return ' ' + value.toLocaleString() + '원';
                    },
                },
            },
        },
        scales: {
            y: {
                beginAtZero: true,
                position: 'left',

                ticks: {
                    callback: function (value) {
                        return value.toLocaleString() + ' 원'; // 숫자 포맷 + "원"
                    },
                },
            },
        },
    },
});

function ajaxChartUpdate(year, month) {
    if (year < memberYear || (year == memberYear && memberMonth > month)) {
        showErrorTitleAlert('잘못된 접근', '잘못된 접근입니다.');
    } else {
        $.ajax({
            url: '/rider/ajaxUpdateChart',
            type: 'POST',
            data: { year, month },
            success: function (response) {
                const updateChartData = response;
                const updateLabels = [month - 2 + '월', month - 1 + '월', month + '월'];
                const updateData = [updateChartData.twoMonthsAgoTotalFee, updateChartData.lastMonthTotalFee, updateChartData.thisMonthTotalFee];
                const updateDataOrders = [updateChartData.twoMonthsAgoOrderCount, updateChartData.lastMonthOrderCount, updateChartData.thisMonthOrderCount];
                myChart.data.labels = updateLabels;
                myChart.data.datasets[0].data = updateData;
                chart.data.datasets[0].data = updateDataOrders;
                chart.update();
                myChart.update(); // 🔥 갱신
            },
            error: function (xhr, status, message) {},
        });
    }
}
