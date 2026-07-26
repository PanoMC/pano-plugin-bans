<script>
  import ApiUtil from '@panomc/sdk/utils/api';
  import { _, showSuccessToast, showErrorToast } from '../main';

  export let addon;

  let config = addon?.config || {
    avatarSize: 'PX_64',
    showAvatars: true,
    showReason: true,
    showBannedBy: true,
    showDuration: true,
    showExpiry: true,
    showHistory: false,
    showTotalBans: true,
    showSearch: true,
    viewLayout: 'LIST',
    paginationSize: 20
  };

  let saving = false;
  let initialConfig = JSON.parse(JSON.stringify(config));

  $: hasChanges = JSON.stringify(config) !== JSON.stringify(initialConfig);

  async function save() {
    if (saving) return;
    saving = true;
    try {
      await ApiUtil.post({ path: '/api/panel/bans/config', body: config });
      if (addon) addon.config = config;
      initialConfig = JSON.parse(JSON.stringify(config));
      showSuccessToast($_('bans.settings.saved'));
    } catch (e) {
      showErrorToast($_('bans.settings.failed'));
      console.error(e);
    } finally {
      saving = false;
    }
  }
</script>

{#if addon?.id === 'pano-plugin-bans'}
  <div class="card">
    <div class="card-header">
      {$_('bans.settings.title')}
    </div>
    <div class="card-body">
      
      <!-- Show Avatars -->
      <div class="row mb-3">
        <label class="col-md-6 col-form-label" for="showAvatars">
          <span class="d-block">
            {$_('bans.settings.show_avatars')}
          </span>
        </label>
        <div class="col-md-6 d-flex align-items-center">
          <div class="form-check form-switch">
              <input class="form-check-input" type="checkbox" id="showAvatars" bind:checked={config.showAvatars}>
          </div>
        </div>
      </div>

      {#if config.showAvatars}
      <!-- Avatar Size -->
      <div class="row mb-3">
         <label class="col-md-6 col-form-label" for="avatarSize">{$_('bans.settings.avatar_size')}</label>
         <div class="col-md-6">
            <select class="form-select" id="avatarSize" bind:value={config.avatarSize}>
                <option value="PX_16">16px</option>
                <option value="PX_32">32px</option>
                <option value="PX_64">64px</option>
            </select>
         </div>
      </div>
      {/if}

      <!-- Show Reason -->
      <div class="row mb-3">
        <label class="col-md-6 col-form-label" for="showReason">
          <span class="d-block">
            {$_('bans.settings.show_reason')}
          </span>
        </label>
        <div class="col-md-6 d-flex align-items-center">
          <div class="form-check form-switch">
              <input class="form-check-input" type="checkbox" id="showReason" bind:checked={config.showReason}>
          </div>
        </div>
      </div>

      <!-- Show Banned By -->
      <div class="row mb-3">
        <label class="col-md-6 col-form-label" for="showBannedBy">
          <span class="d-block">
            {$_('bans.settings.show_banned_by')}
          </span>
        </label>
        <div class="col-md-6 d-flex align-items-center">
          <div class="form-check form-switch">
              <input class="form-check-input" type="checkbox" id="showBannedBy" bind:checked={config.showBannedBy}>
          </div>
        </div>
      </div>

      <!-- Show Ban Duration -->
      <div class="row mb-3">
        <label class="col-md-6 col-form-label" for="showDuration">
          <span class="d-block">
            {$_('bans.settings.show_duration')}
          </span>
        </label>
        <div class="col-md-6 d-flex align-items-center">
             <div class="form-check form-switch">
                <input class="form-check-input" type="checkbox" id="showDuration" bind:checked={config.showDuration}>
             </div>
        </div>
      </div>

      <!-- Show Expiry -->
      <div class="row mb-3">
        <label class="col-md-6 col-form-label" for="showExpiry">
          <span class="d-block">
            {$_('bans.settings.show_expiry')}
          </span>
        </label>
        <div class="col-md-6 d-flex align-items-center">
             <div class="form-check form-switch">
                 <input class="form-check-input" type="checkbox" id="showExpiry" bind:checked={config.showExpiry}>
             </div>
        </div>
      </div>

      <!-- Show History -->
      <div class="row mb-3">
        <label class="col-md-6 col-form-label" for="showHistory">
          <span class="d-block">
            {$_('bans.settings.show_history')}
          </span>
          <small>
            {$_('bans.settings.show_history_desc')}
          </small>
        </label>
        <div class="col-md-6 d-flex align-items-center">
             <div class="form-check form-switch">
                 <input class="form-check-input" type="checkbox" id="showHistory" bind:checked={config.showHistory}>
             </div>
        </div>
      </div>

      <!-- Show Total Bans -->
      <div class="row mb-3">
        <label class="col-md-6 col-form-label" for="showTotalBans">
          <span class="d-block">
            {$_('bans.settings.show_total_bans')}
          </span>
        </label>
        <div class="col-md-6 d-flex align-items-center">
             <div class="form-check form-switch">
                 <input class="form-check-input" type="checkbox" id="showTotalBans" bind:checked={config.showTotalBans}>
             </div>
        </div>
      </div>

      <!-- Show Search -->
      <div class="row mb-3">
        <label class="col-md-6 col-form-label" for="showSearch">
          <span class="d-block">
            {$_('bans.settings.show_search')}
          </span>
        </label>
        <div class="col-md-6 d-flex align-items-center">
             <div class="form-check form-switch">
                 <input class="form-check-input" type="checkbox" id="showSearch" bind:checked={config.showSearch}>
             </div>
        </div>
      </div>

      <!-- View Layout -->
      <div class="row mb-3">
        <label class="col-md-6 col-form-label" for="viewLayout1">
          {$_('bans.settings.view_layout')}
        </label>
        <div class="col-md-6">
          <div class="btn-group w-100" role="group">
            <input
              type="radio"
              class="btn-check"
              name="viewLayout"
              id="viewLayout1"
              value="LIST"
              bind:group={config.viewLayout} />
            <label class="btn btn-outline-primary" for="viewLayout1">
              {$_('bans.settings.view_layout_list')}
            </label>

            <input
              type="radio"
              class="btn-check"
              name="viewLayout"
              id="viewLayout2"
              value="GRID"
              bind:group={config.viewLayout} />
            <label class="btn btn-outline-primary" for="viewLayout2">
              {$_('bans.settings.view_layout_grid')}
            </label>
          </div>
        </div>
      </div>

      <!-- Pagination Size -->
      <div class="row mb-3">
           <label class="col-md-6 col-form-label" for="paginationSize">{$_('bans.settings.pagination_size')}</label>
           <div class="col-md-6">
               <input type="number" class="form-control" id="paginationSize" bind:value={config.paginationSize} min="1" max="100">
           </div>
      </div>

      <div class="mt-4">
        <button class="btn btn-secondary" on:click={save} disabled={saving || !hasChanges}>
          {#if saving}
            <span class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
          {/if}
          {$_('save')}
        </button>
      </div>
    </div>
  </div>
{/if}

