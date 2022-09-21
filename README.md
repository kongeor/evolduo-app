# TODO

## Check deps

```clj
clojure -Sdeps '{:deps {com.github.liquidz/antq {:mvn/version "RELEASE"}}}' -M -m antq.core
```

## Release checklist ongoing

- [ ] Chickn tweaks
- [ ] Terms, License, Dev blog
- [ ] Optimise fitness

## Release checklist

- [X] Secure cookie
- [X] Raw fitness
- [X] Fix chickn
- [ ] Contact page
- [ ] Known issues (mobile view, emails, fixed C)
- [X] List limits
- [ ] Update deps / abc.js
- [ ] Help Texts refinements (+ invitation)
- [ ] Evolution more details page
- [X] Player fixes
- [X] Wizard/Presets
- [ ] Roadmap
- [X] Humanize dates
- [ ] Sponsors
- [ ] Readme
- [X] Note split
- [X] You need to be logged in to create an evolution
- [X] Remove initial iterations
- [X] Debugging info (hardcoded)
- [ ] Review maybe-fix
- [X] Cookie persistence
- [X] Shuffle chromosome list
- [X] Favico
- [X] Subresource integrity
- [X] Validate 0 crossover 0 mutation, improve selections
- [ ] Bound checks, more notes etc.
- [X] Progress hover
- [ ] Iteration navigation
- [ ] Rating past tracks
- [ ] Explanations on each page
- [ ] Enable download buttons after listening the track
- [ ] Terms downloading staff from GH
- [ ] Limits/verified rating, evolutions (check role)
- [ ] Validate rating


## Stylistic

- [ ] Hide no pagination
- [ ] Download dropdown + same row (on the right)
- [ ] Evolution help text

## Unclear

- [ ] Explorer chromosome selection (for debugging chromatic asc/desc, blank for practice etc.)

## Post-release checklist

- [ ] Captcha PR
- [ ] W3C check
- [ ] Zaproxy check
- [ ] Account likes
- [ ] Progress indicator
- [ ] SEO
- [ ] Explorer track options (chromatic etc.)
 
## Post-stats checklist

- [ ] User settings (instrument, debugging info)


## The road to alpha

- [ ] Explorer chord construction
- [ ] Repetitions
- [ ] Instrument 
- [ ] Logging config
- [ ] Generate a random chromosome based on mode and progression
- [ ] Show the list of chromosomes per evolution iteration
- [ ] Delete old code, upgrade deps
- [ ] Moar modes and chord progression
- [ ] Upvote/Downvote a track
- [ ] Add missing fields: Evolution(abc progression, evolve after, mode), Chromosome(version)
- [ ] Limits
- [ ] Add background task to evolve tracks
- [ ] Indexes, indexes, indexes
- [ ] Mails and templates
- [ ] Invite a user to collaborate on a private track
- [ ] Password reset
- [ ] SSO
- [ ] Home screen
- [ ] Footer
- [ ] Docker
- [ ] Deploy

## Bugs

- [ ] Evolution can be created without a user
- [ ] Tempo not taken into account
- [ ] Sharps/Flats/Naturals
- [ ] A 7 chord out of range

## Questions

- [ ] Initial evolution not needed
- [ ] Pop sizes
- [ ] Rating policies

## Support

- Use the app
- Ringtones
- Sponsors

## Thanks

https://favicon.io/favicon-generator/