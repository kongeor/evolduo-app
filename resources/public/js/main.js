// navbar js toggle
document.addEventListener('DOMContentLoaded', () => {

  // Get all "navbar-burger" elements
  const $navbarBurgers = Array.prototype.slice.call(document.querySelectorAll('.navbar-burger'), 0);

  // Add a click event on each of them
  $navbarBurgers.forEach( el => {
    el.addEventListener('click', () => {

      // Get the target from the "data-target" attribute
      const target = el.dataset.target;
      const $target = document.getElementById(target);

      // Toggle the "is-active" class on both the "navbar-burger" and the "navbar-menu"
      el.classList.toggle('is-active');
      $target.classList.toggle('is-active');

    });
  });


  // enable bulma-slider.min.js

  bulmaSlider.attach();

  // enable dropdowns

//  const $dropdowns = Array.prototype.slice.call(document.querySelectorAll('.dropdown'), 0);
//  $dropdowns.forEach (el => {
//      el.addEventListener('click', (event) => {
//
//
//        console.log(el.dataset)
//
//        const target = el.dataset.target;
//
//        const $target = document.getElementById(target);
//
//        console.log($target)
//
//        event.stopPropagation();
//        $target.classList.toggle('is-active');
//      });
//  })

    const cookieConsent = new CookieConsent({
        contentUrl: "/js/cookie-consent-content", // location of the language files
        privacyPolicyUrl: "/privacy-policy",
        postSelectionCallback: () => {
            if (cookieConsent.trackingAllowed()) {
                // I'll not bother injecting html for now
                location.reload();
            }
        }
    })

});