// Plugin-level options of the Pano plugin kit (@panomc/plugin-kit). The namespace is `bans`
// (the plugin id minus `pano-plugin-`); the only view file sits in src/theme, so none moved.
export default {
  viewDirs: ['src/theme'],
  styles: {
    // The inline sizes of BansPage (search box 300 px, reason badge max 250 px, the pointer-events of the search icon) stay
    // inline: plugin.css is layered, so Bootstrap's unlayered `.form-control { width: 100% }` would win over a class.
    styleAttrAllow: ['BansPage'],
  },
};
