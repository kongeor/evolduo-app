# TODO

## Check deps

```clj
clojure -Sdeps '{:deps {com.github.liquidz/antq {:mvn/version "RELEASE"}}}' -M -m antq.core
```

## Release checklist ongoing

- [ ] Chickn tweaks
- [ ] Terms, License, Dev blog

## Release checklist

- [X] Secure cookie
- [ ] Raw fitness
- [X] Fix chickn
- [ ] About page
- [ ] Contact page
- [ ] Known issues (mobile view, emails, fixed C)
- [X] List limits
- [ ] Update deps
- [ ] Help Texts
- [ ] Evolution details page
- [ ] Player fixes
- [ ] Wizard/Presets
- [ ] Optimise fitness
- [ ] Roadmap
- [X] Humanize dates
- [ ] Sponsors
- [ ] Readme
- [ ] Note split
- [ ] You need to be logged in to create an evolution
- [ ] Remove initial iterations
- [ ] Debugging info (hardcoded)

## Post-release checklist

- [ ] Captcha PR
- [ ] W3C check
- [ ] Zaproxy check
 
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
