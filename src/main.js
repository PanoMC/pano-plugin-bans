import { PanoPlugin } from '@panomc/sdk';
import { derived } from 'svelte/store';
import { _ as i18n } from '@panomc/sdk/utils/language';
import { viewComponent } from '@panomc/sdk/utils/component';
import ApiUtil from '@panomc/sdk/utils/api';
import { showToast } from '@panomc/sdk/toasts';

const pluginId = 'pano-plugin-bans';

// this is to render plugin translations
export const _ = derived(i18n, ($_fn) => {
  return (key, options) => $_fn(`plugins.${pluginId}.${key}`, options);
});

// Success/failure colouring for this plugin's toasts, matching the panel. showToast from
// @panomc/sdk/toasts is the host panel's ToastContainer `show`, whose signature is
// (text, params, toastComponent, options): passing undefined for toastComponent keeps the
// host's DefaultToast, and options.variant maps to Bootstrap's text-success / text-danger.
// These live here rather than in @panomc/sdk/toasts because this plugin is pinned to
// @panomc/sdk 1.0.0-dev.39, which predates the variants; they can be dropped for a direct
// SDK import once that pin moves. On an older panel build the extra argument is ignored and
// the toast renders neutral, so this degrades instead of breaking.
export function showSuccessToast(text, params = {}) {
  return showToast(text, params, undefined, { variant: 'success' });
}

export function showErrorToast(text, params = {}) {
  return showToast(text, params, undefined, { variant: 'danger' });
}

export default class BansPlugin extends PanoPlugin {
  onLoad() {
    const pano = this.pano;

    if (pano.isPanel) {
      pano.ui.addon.onLoad(async (data, event) => {
        if (data.addon.id !== pluginId) return;

        try {
          const res = await ApiUtil.get({
            path: '/api/panel/bans/config',
            request: event,
          });
          data.addon.config = res.config;
        } catch (e) {
          console.error('[pano-plugin-bans] Failed to load config', e);
        }
      });

      pano.ui.hook.register({
        name: `panel:plugin-detail:content:${pluginId}`,
        component: viewComponent(() => import('./panel/BansSettings.svelte')),
        permission: `pano.plugin.${pluginId}.manage.bans`,
      });
    } else {
      pano.ui.page.register({
        path: '/bans',
        component: viewComponent(() => import('./theme/BansPage.svelte')),
      });

      pano.ui.nav.site.editNavLinks((navItems) => {
        if (!navItems.find((n) => n.href === '/bans')) {
          navItems.push({
            href: '/bans',
            text: `plugins.${pluginId}.bans.title`,
            target: '_self',
            startsWith: false,
          });
        }
        return navItems;
      });
    }
  }

  onContextUpdate(ctx) { }

  onUnload() { }
}
