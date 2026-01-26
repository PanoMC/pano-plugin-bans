<div class="container vstack gap-3">
  <PageTitle>
    <span slot="title">
      {#if config.showTotalBans && pagination.total > 0}
        {$_('bans.count_title', { values: {count: pagination.total }})}
      {:else}
        {$_('bans.title')}
      {/if}
    </span>
  </PageTitle>

  {#if config.showSearch}
    <div class="d-flex justify-content-center">
      <div class="position-relative">
        <div
          class="position-absolute top-50 start-0 translate-middle-y ms-3 z-3 text-muted"
          style="pointer-events: none;">
          {#if isSearching}
            <div class="spinner-border spinner-border-sm text-primary" role="status"></div>
          {:else}
            <i class="bi bi-search"></i>
          {/if}
        </div>
        <input
          type="text"
          class="form-control rounded-pill ps-5"
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
    <div class:row={config.viewLayout === 'GRID'} class:list-group={config.viewLayout === 'LIST'}>
      {#each bans as ban}
        {#if config.viewLayout === 'GRID'}
          <div class="col-md-6 col-lg-4 mb-3">
            <a
              href={`/player/${ban.username}`}
              class="card h-100 text-decoration-none text-body transition-transform">
              <div class="card-body text-center">
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
                <h5 class="card-title">
                  {ban.username}
                </h5>

                {#if config.showReason}
                  <p class="card-text">
                    <span class="badge bg-danger"
                      >{$_('bans.reason')}: {ban.banMessage || 'N/A'}</span>
                  </p>
                {/if}

                <p class="mb-0">
                  {#if config.showDuration && ban.banDate}
                    <i class="bi bi-clock"></i>
                    {$_('bans.banned_on')}: <PanoDate time={ban.banDate} /><br />
                  {/if}
                  {#if config.showExpiry}
                    <i class="bi bi-calendar-x"></i>
                    {$_('bans.expires')}:
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
            class="list-group-item list-group-item-action d-flex align-items-center text-decoration-none text-body">
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
                  <span class="badge text-bg-danger focus-ring me-2"
                    >{$_('bans.reason')}: {ban.banMessage || 'N/A'}</span>
                {/if}
              </div>

              <small class="text-gray">
                {#if config.showDuration && ban.banDate}
                  <span class="me-3">
                    <i class="bi bi-clock"></i>
                    {$_('bans.banned_on')}: <PanoDate time={ban.banDate} />
                  </span>
                {/if}
                {#if config.showExpiry}
                  <span>
                    <i class="bi bi-calendar-x"></i>
                    {$_('bans.expires')}:
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
  import ApiUtil, {buildQueryParams} from '@panomc/sdk/utils/api';

  export async function load(event) {
    const {
      url: { searchParams },
    } = event;
    const page = parseInt(searchParams.get('page') || '1');
    const search = searchParams.get('search') || '';

    try {
      const queryParams = buildQueryParams({ page, search });
      const res = await ApiUtil.get({
        path: `/api/bans${queryParams}`,
        request: event,
      });

      return {
        data: {
          bans: res.bans,
          config: res.config,
          pagination: res.pagination,
          search,
        },
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
      };
    }
  }
</script>

<script>
  import { onMount } from 'svelte';
  import { goto, page } from '@panomc/sdk/svelte';
  import { _ } from '../main';
  import {
    Pagination,
    Date as PanoDate,
    PlayerHead,
    NoContent,
    PageTitle,
  } from '@panomc/sdk/components/theme';

  export let data;
  const { bans, config, pagination, search } = data;

  let searchInput = search;
  let searchTimeout;
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
      goto(url.toString(), { keepFocus: true });
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
