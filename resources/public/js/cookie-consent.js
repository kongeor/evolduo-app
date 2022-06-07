const languages = {
  en: {
    title: 'Cookies policy',
    content: 'Our website uses cookies to analyse how the site is used and to ensure your experience is consistent between visits.',
    accept: 'Accept',
    learnMore: 'Learn more',
  }
}

const createCookie = (name, value, days, domain) => {
  let expires;

  if (days) {
    const date = new Date();
    date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000));
    expires = `; expires=${date.toGMTString()}`;
  } else {
    expires = '';
  }

  document.cookie = `${name}=${value}${expires}; SameSite=Strict; path=/; domain=${domain}`;
};

const readCookie = (name) => {
  const nameQuery = `${name}=`;
  const cookies = document.cookie.split(';');
  for (let i = 0; i < cookies.length; i += 1) {
    let cookie = cookies[i];

    while (cookie.charAt(0) === ' ') {
      cookie = cookie.substring(1, cookie.length);
    }

    if (cookie.indexOf(nameQuery) === 0) {
      return cookie.substring(nameQuery.length, cookie.length);
    }
  }

  return null;
};

let COOKIE_BOX_INITIALIZED = false;

class CookieBox {
  constructor(params) {
    COOKIE_BOX_INITIALIZED = true;
    const userSettings = window.CookieBoxConfig || params || {};

    this.box = document.createElement('div');
    this.box.className = 'cookie-box';
    this.settings = {
      backgroundColor: userSettings.backgroundColor || '#3ea25f',
      textColor: userSettings.textColor || '#fff',
      language: userSettings.language || 'en',
      containerWidth: userSettings.containerWidth || 1140,
      url: userSettings.url || '/privacy-policy',
      linkTarget: userSettings.linkTarget || '_blank',
      cookieKey: userSettings.cookieKey || 'cookie-box',
      cookieDomain: userSettings.cookieDomain || document.location.hostname,
      cookieExpireInDays: userSettings.cookieExpireInDays || 365,
      content: userSettings.content || {},
    };
    this.dictionary = languages[this.settings.language];
  }

  init() {
    if (!this.dictionary) {
      console.error(`${this.settings.language} language is not supported`);

      return;
    }

    if (readCookie(this.settings.cookieKey)) {
      return;
    }

    this.show();

    document.querySelector('.cookie-box__button').addEventListener('click', () => this.hide());
  }

  render() {
    const { settings } = this;

    return `
    <div style="background-color: ${settings.backgroundColor}; color: ${settings.textColor}">
      <div class="cookie-box__inner" style="max-width: ${settings.containerWidth}px; margin: 0 auto">
        <div class="cookie-box__content">
          <div class="cookie-box__icon">
            <svg xmlns="http://www.w3.org/2000/svg" width="44" height="46" viewBox="0 0 44 46"><path fill-rule="evenodd" fill-opacity="0.6" fill="currentColor" d="M42.841 24.2a3.933 3.933 0 0 1-5.573 0 3.998 3.998 0 0 1 0-5.607 3.939 3.939 0 0 1 5.573 0l.05.056v-.022a3.993 3.993 0 0 1-.05 5.573zm-1.288-4.245l-.039-.039-.001-.001a2.087 2.087 0 0 0-2.963.001 2.123 2.123 0 0 0 0 2.983 2.091 2.091 0 0 0 2.964 0 2.123 2.123 0 0 0 .039-2.944zm-1.566 15.443l.558.157a.94.94 0 0 1 .44 1.525 22.858 22.858 0 0 1-16.358 8.856C12.02 46.9 1.024 37.398.066 24.713-.892 12.028 8.551.963 21.158-.001a.932.932 0 0 1 .964.662c.273.712.563 1.452.88 2.142.221.535.508 1.039.853 1.503a17.208 17.208 0 0 0 4.33 3.448c2.836 1.761 5.216 3.236 5.433 8.282a10.412 10.412 0 0 1-.669 3.684c-.574 1.822-1.142 3.633.624 6.044.842 1.178.976 2.524 1.115 3.926.217 2.175.451 4.553 4.597 5.539l.702.169zm-7.127-5.507c-.111-1.138-.212-2.231-.769-2.977-2.324-3.196-1.616-5.456-.903-7.716.37-.974.568-2.007.586-3.05-.173-4.054-2.168-5.293-4.548-6.773a18.803 18.803 0 0 1-4.77-3.824 8.661 8.661 0 0 1-1.142-1.941c-.234-.515-.468-1.121-.697-1.682a20.969 20.969 0 0 0-6.971 2.04C3.22 9.119-1.081 21.8 4.038 32.29c5.12 10.491 17.722 14.819 28.148 9.668h-.011a21.11 21.11 0 0 0 6.464-4.945c-5.222-1.324-5.5-4.318-5.779-7.122zm-3.021 9.41a2.664 2.664 0 0 1-3.783-.006l-.006-.007a2.703 2.703 0 0 1 .006-3.806 2.666 2.666 0 0 1 1.895-.791l-.006-.005a2.676 2.676 0 0 1 1.891.786 2.715 2.715 0 0 1 .009 3.823l-.006.006zm-1.334-2.498a.822.822 0 0 0-1.167.004.833.833 0 0 0 .004 1.173.82.82 0 0 0 1.166-.003h.022a.836.836 0 0 0 .218-.561.84.84 0 0 0-.243-.613zm-4.483-17.902l-2.296-1.682a.948.948 0 0 1-.198-1.32.935.935 0 0 1 1.313-.199l2.296 1.682a.947.947 0 0 1 .197 1.32.934.934 0 0 1-1.312.199zm-8.169 11.344l-.056-.062a3.087 3.087 0 0 1 .056-4.295 3.045 3.045 0 0 1 4.324 0h.006a3.096 3.096 0 0 1 0 4.357 3.049 3.049 0 0 1-4.33 0zm3.003-3.045a1.197 1.197 0 0 0-1.671 0 1.216 1.216 0 0 0-.034 1.682l.039.034a1.193 1.193 0 0 0 1.672 0l-.006-.034a1.21 1.21 0 0 0 0-1.682zm.814-17.136a2.412 2.412 0 0 1-3.379-.058 2.456 2.456 0 0 1 .002-3.457l.01-.011a2.413 2.413 0 0 1 3.43.013 2.454 2.454 0 0 1-.002 3.457l-.061.056zM18.416 7.85a.568.568 0 0 0-.808 0l.028.028a.564.564 0 0 0 0 .813.556.556 0 0 0 .78 0v-.028a.578.578 0 0 0 0-.813zm-.797 31.097c.417.31.506.901.198 1.32a.935.935 0 0 1-1.312.2l-2.296-1.683a.947.947 0 0 1-.198-1.32.934.934 0 0 1 1.312-.199l2.296 1.682zm-4.087-23.233a.934.934 0 0 1-1.034-.822l-.334-2.837a.935.935 0 0 1 .816-1.04.932.932 0 0 1 1.034.821l.334 2.837a.936.936 0 0 1-.816 1.041zm-1.903 6.137l-.01.01c-.95.95-2.485.945-3.429-.01l-.01-.011a2.45 2.45 0 0 1 .01-3.449l.011-.01a2.413 2.413 0 0 1 3.428.01l.01.01a2.45 2.45 0 0 1-.01 3.45zm-1.313-2.111a.555.555 0 0 0-.788-.008.563.563 0 0 0-.008.793.555.555 0 0 0 .788.008.562.562 0 0 0 .008-.793zm-2.482 5.413l.334 2.838a.936.936 0 0 1-.816 1.04.934.934 0 0 1-1.034-.822l-.334-2.837a.935.935 0 0 1 .816-1.04.932.932 0 0 1 1.034.821zM34.387 7.81a2.412 2.412 0 0 1-3.377-.056l-.002-.002a2.456 2.456 0 0 1 .002-3.457 2.415 2.415 0 0 1 3.441.002 2.455 2.455 0 0 1-.003 3.457l-.061.056zm-1.204-2.118a.554.554 0 0 0-.787-.02.563.563 0 0 0-.021.793.556.556 0 0 0 .78 0v-.028l.028.028a.563.563 0 0 0 0-.773z"/></svg>
          </div>
          <div class="cookie-box__content__inner">
            <p class="cookie-box__title">${settings.content.title || languages[settings.language].title}</p>
            <div class="cookie-box__desc">
              ${settings.content.content || languages[settings.language].content}
              ${settings.url ? `<a href="${settings.url}" target="${settings.linkTarget}">${settings.content.learnMore || languages[settings.language].learnMore} &raquo;</a>` : ''}
            </div>
            </div>
          </div>
        <div class="cookie-box__buttons">
          <button class="cookie-box__button">
          <span>${settings.content.accept || languages[settings.language].accept}</span>
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="17" viewBox="0 0 18 17"><path fill-rule="evenodd" fill="currentColor" d="M17.756 2.096l-8.597 8.5a.784.784 0 0 1-.553.226c-.2 0-.4-.075-.553-.226L5.709 8.278a.765.765 0 0 1 0-1.093.787.787 0 0 1 1.105 0l1.792 1.772 8.045-7.953a.787.787 0 0 1 1.105 0 .766.766 0 0 1 0 1.092zm-6.287.052a7.065 7.065 0 0 0-2.859-.603h-.004c-3.877 0-7.032 3.117-7.034 6.951-.002 3.834 3.151 6.956 7.03 6.959h.004c3.877 0 7.032-3.118 7.034-6.951v-.718c0-.427.35-.773.782-.773.432 0 .781.346.781.773v.718C17.201 13.19 13.344 17 8.606 17h-.005C3.86 16.997.006 13.181.009 8.495.011 3.81 3.868 0 8.606 0h.005a8.629 8.629 0 0 1 3.495.736.77.77 0 0 1 .395 1.02.786.786 0 0 1-1.032.392z"/></svg>            </button>
        </div>
      </div>
    </div>
    `;
  }

  show() {
    this.box.innerHTML = this.render();
    document.body.appendChild(this.box);
  }

  hide() {
    this.box.classList.add('hidden');
    createCookie(
      this.settings.cookieKey,
      true,
      this.settings.cookieExpireInDays,
      this.settings.cookieDomain,
    );

    setTimeout(() => {
      this.box.remove();
    }, 800);
  }
}

if (typeof window !== 'undefined') {
  const initBox = setInterval(() => {
    if (document.readyState === 'complete') {
      clearInterval(initBox);
      if (!COOKIE_BOX_INITIALIZED) {
        new CookieBox().init();
      }
    }
  }, 50);
}