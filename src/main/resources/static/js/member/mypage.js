// ==============================
// 마이페이지 공용 무한스크롤
// ==============================
// containerSel : 무한스크롤 대상 컨테이너 셀렉터 (ex: ".wish-grid")
// itemSel      : 아이템 셀렉터 (ex: ".wish-item")
// batchSize    : 한 번에 보여줄 개수 (기본 5개)
function initInfiniteScroll(containerSel, itemSel, batchSize = 5, ns = "mypageScroll") {
  const $container = $(containerSel);
  if ($container.length === 0) return;

  const $items = $container.find(itemSel);
  if ($items.length === 0) return;

  let shownCount = 0;

  // 초기 상태
  $items.hide();
  $items.slice(0, batchSize).fadeIn();
  shownCount = batchSize;

  // 스크롤 이벤트 등록 (네임스페이스 별도 관리)
  $(window).off("scroll." + ns).on("scroll." + ns, function () {
    if ($(window).scrollTop() + $(window).height() >= $(document).height() - 100) {
      $items.slice(shownCount, shownCount + batchSize).fadeIn();
      shownCount += batchSize;

      if (shownCount >= $items.length) {
        $(window).off("scroll." + ns);
      }
    }
  });
}

// ==============================
// 자동 실행 (페이지마다 자동 적용)
// ==============================
$(document).ready(function () {
  // 찜 목록
  if ($(".wish-grid").length > 0) {
    initInfiniteScroll(".wish-grid", ".wish-item", 5, "wishScroll");
  }

  // 주문 내역
  if ($(".order-list").length > 0) {
    initInfiniteScroll(".order-list", ".order-item", 5, "orderScroll");
  }

  // 정기결제
  if ($(".subs-list").length > 0) {
    initInfiniteScroll(".subs-list", ".subs-item", 5, "subsScroll");
  }

  // 후기
  if ($(".review-list").length > 0) {
    initInfiniteScroll(".review-list", ".review-card", 5, "reviewScroll");
    $(".pagination").hide();
  }
  
  // 후기
  if ($(".product-qna").length > 0) {
    initInfiniteScroll(".product-qna", ".product-card", 5, "p");
    $(".pagination").hide();
  }
});
