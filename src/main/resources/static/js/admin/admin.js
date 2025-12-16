// 사이드바 하위메뉴 토글 (클릭 + 하나만 열리기)
document.querySelectorAll(".has-sub > .menu-btn").forEach(btn => {
  btn.addEventListener("click", () => {
    const submenu = btn.nextElementSibling;

    // 다른 모든 하위 메뉴 닫기
    document.querySelectorAll(".has-sub > .submenu").forEach(sm => {
      if (sm !== submenu) sm.style.display = "none";
    });

    // 현재 클릭한 메뉴만 토글
    submenu.style.display = submenu.style.display === "flex" ? "none" : "flex";
  });
});