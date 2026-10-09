<div class="bans-bans-page container vstack gap-3">
  {#if config.showSearch}
    <div class="d-flex justify-content-center">
      <div class="position-relative">
        <div
          class="position-absolute top-50 start-0 translate-middle-y ms-3 z-3 text-muted"
          style="pointer-events: none;">
          {#if isSearching}
            <div class="spinner-border spinner-border-sm text-primary" role="status"></div>
          {:else}
            <i class="fa-solid fa-magnifying-glass"></i>
          {/if}
        </div>
        <input
          type="text"
          class="bans-bans-page__input form-control rounded-pill ps-5"
          style="width: 300px;"
          placeholder={$_('bans.search_placeholder')}
          bind:value={searchInput}
          on:input={handleSearch} />
      </div>
    </div>
  {/if}
  {#if bans.length === 0}
    <NoContent text={searchInput ? $_('bans.no_results') : $_('bans.empty')} />
  {:else}
    <div class="bans-bans-page__list" class:row={isGrid} class:list-group={isList}>
      {#each bans as ban}
        {#if config.viewLayout === 'GRID'}
          <div class="col-md-6 col-lg-4 mb-3">
            <a
              href={`/player/${ban.username}`}
              class="card h-100 text-decoration-none">
              <div class="bans-bans-page__body card-body text-center">
                {#if config.showAvatars}
                  <div class="mb-3">
                    <PlayerHead
                      username={ban.username}
                      width={config.avatarSize === 'PX_16'
                        ? 16
                        : config.avatarSize === 'PX_32'
                          ? 32
                          : 64}
                      height={config.avatarSize === 'PX_16'
                        ? 16
                        : config.avatarSize === 'PX_32'
                          ? 32
                          : 64}
                      {checkTime} />
                  </div>
                {/if}
                <h5 class="bans-bans-page__title card-title">
                  {ban.username}
                </h5>

                {#if config.showReason}
                  <p class="card-text">
                    <span
                      class="bans-bans-page__badge badge text-bg-danger text-truncate"
                      style="max-width: 250px; vertical-align: middle;"
                      use:tooltip={[$_('bans.reason') + ': ' + (ban.banMessage || 'N/A')]}
                      >{$_('bans.reason')}: {ban.banMessage || 'N/A'}</span
                    >
                  </p>
                {/if}

                <p class="mb-0">
                  {#if config.showDuration && ban.banDate}
                    <i
                      class="fa-regular fa-clock me-1"
                      use:tooltip={[$_('bans.banned_on')]}
                      aria-label={$_('bans.banned_on')}></i>
                    <PanoDate time={ban.banDate} /><br />
                  {/if}
                  {#if config.showExpiry}
                    <i
                      class="fa-regular fa-calendar-xmark me-1"
                      use:tooltip={[$_('bans.expires')]}
                      aria-label={$_('bans.expires')}></i>
                    {#if ban.bannedUntil}
                      <PanoDate time={ban.bannedUntil} />
                    {:else}
                      {$_('bans.permanent')}
                    {/if}
                  {/if}
                </p>
              </div>
            </a>
          </div>
        {:else}
          <a
            href={`/player/${ban.username}`}
            class="bans-bans-page__item list-group-item list-group-item-action d-flex align-items-center">
            {#if config.showAvatars}
              <div class="me-3">
                <PlayerHead
                  username={ban.username}
                  width={config.avatarSize === 'PX_16'
                    ? 16
                    : config.avatarSize === 'PX_32'
                      ? 32
                      : 64}
                  height={config.avatarSize === 'PX_16'
                    ? 16
                    : config.avatarSize === 'PX_32'
                      ? 32
                      : 64}
                  lastActivityTime={ban.lastActivityTime}
                  {checkTime} />
              </div>
            {/if}

            <div class="flex-grow-1">
              <div class="d-flex justify-content-between align-items-center">
                <h5>{ban.username}</h5>
              </div>

              <div class="mb-2">
                {#if config.showReason}
                  <span
                    class="bans-bans-page__reason badge text-bg-danger text-truncate focus-ring me-2"
                    style="max-width: 250px; vertical-align: middle;"
                    use:tooltip={[$_('bans.reason') + ': ' + (ban.banMessage || 'N/A')]}
                    >{$_('bans.reason')}: {ban.banMessage || 'N/A'}</span
                  >
                {/if}
              </div>

              <small>
                {#if config.showDuration && ban.banDate}
                  <span class="me-3">
                    <i
                      class="fa-regular fa-clock me-1"
                      use:tooltip={[$_('bans.banned_on')]}
                      aria-label={$_('bans.banned_on')}></i>
                    <PanoDate time={ban.banDate} />
                  </span>
                {/if}
                {#if config.showExpiry}
                  <span>
                    <i
                      class="fa-regular fa-calendar-xmark me-1"
                      use:tooltip={[$_('bans.expires')]}
                      aria-label={$_('bans.expires')}></i>
                    {#if ban.bannedUntil}
                      <PanoDate time={ban.bannedUntil} />
                    {:else}
                      {$_('bans.permanent')}
                    {/if}
                  </span>
                {/if}
              </small>
            </div>
          </a>
        {/if}
      {/each}
    </div>
  {/if}
  <!-- Pagination -->
  {#if pagination.total > 0}
    <Pagination
      page={pagination.current}
      total={pagination.last}
      on:change={(e) => loadBans(e.detail)} />
  {/if}
</div>

<script context="module">
  export const view = { path: '/bans' };
  import { buildQueryParams } from '@panomc/sdk/utils/api';
  import { api } from '@panomc/sdk/plugin-api';

  export async function load(event) {
    const {
      url: { searchParams },
    } = event;
    const page = parseInt(searchParams.get('page') || '1');
    const search = searchParams.get('search') || '';

    try {
      const queryParams = buildQueryParams({ page, search });
      const res = await api.get({
        path: `/bans${queryParams}`,
        request: event,
      });

      // The list answers { items, page }; an older core answered { bans, pagination } (read until CX-15).
      const bans = res.items ?? res.bans ?? [];
      const pagination = res.page
        ? {
            current: res.page.number,
            last: res.page.totalPages,
            total: res.page.totalItems,
            perPage: res.page.size,
          }
        : res.pagination;

      return {
        data: {
          bans,
          config: res.config,
          pagination,
          search,
        },
        pageTitle:
          res.config?.showTotalBans && pagination?.total > 0
            ? {
                title: 'plugins.pano-plugin-bans.bans.count_title',
                titleValues: { count: pagination.total },
              }
            : 'plugins.pano-plugin-bans.bans.title',
      };
    } catch (e) {
      console.error(e);
      return {
        data: {
          bans: [],
          config: {},
          pagination: { current: 1, last: 1, total: 0, perPage: 20 },
          search: '',
        },
        pageTitle: 'plugins.pano-plugin-bans.bans.title',
      };
    }
  }
</script>

<script>
  import { onMount } from 'svelte';
  import { goto, page } from '@panomc/sdk/svelte';
  import { derived } from 'svelte/store';
  import { _ as i18n } from '@panomc/sdk/utils/language';
  import {
    Pagination,
    Date as PanoDate,
    PlayerHead,
    NoContent,
  } from '@panomc/sdk/components/theme';
  import tooltip from '@panomc/sdk/utils/tooltip';

  // plugin translations: `$_('key')` reads `plugins.pano-plugin-bans.key`
  const _ = derived(i18n, ($_fn) => (key, options) => $_fn(`plugins.pano-plugin-bans.${key}`, options));

  export let data;
  $: ({ bans, config, pagination, search } = data);
  $: isGrid = config.viewLayout === 'GRID';
  $: isList = config.viewLayout === 'LIST';

  let searchInput = search;
  let searchTimeout;
  $: searchInput = search;
  let isSearching = false;

  $: if (data) {
    isSearching = false;
  }

  function handleSearch() {
    isSearching = true;
    clearTimeout(searchTimeout);
    searchTimeout = setTimeout(() => {
      const url = new URL($page.url);
      url.searchParams.set('search', searchInput);
      url.searchParams.set('page', '1');
      goto(url.toString(), { keepFocus: true, noscroll: true });
    }, 500);
  }

  async function loadBans(pageNum) {
    const url = new URL($page.url);
    url.searchParams.set('page', pageNum.toString());
    await goto(url.toString());
  }

  let checkTime = 0;
  let interval;

  onMount(() => {
    interval = setInterval(() => {
      checkTime += 1;
    }, 1000);

    return () => {
      if (interval) {
        clearInterval(interval);
      }
    };
  });
</script>
