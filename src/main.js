import {PanoPlugin} from '@panomc/sdk';
import {derived} from 'svelte/store';
import {_ as i18n} from '@panomc/sdk/utils/language';
import {viewComponent} from '@panomc/sdk/utils/component';
import ApiUtil from '@panomc/sdk/utils/api';

const pluginId = 'pano-plugin-bans';

// this is to render plugin translations
export const _ = derived(i18n, ($_fn) => {
  return (key, options) => $_fn(`plugins.${pluginId}.${key}`, options);
});

export default class BansPlugin extends PanoPlugin {
  onLoad() {
    const pano = this.pano;

    if (pano.isPanel) {
      pano.ui.addon.onLoad(async (data, event) => {
        if (data.addon.id !== 'pano-plugin-bans') return;

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
        name: 'panel:plugin-detail:content',
        component: viewComponent(() => import('./panel/BansSettings.svelte')),
        permission: 'pano.plugin.bans.manage', // Assuming generic permission or consistent with defined permission
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
