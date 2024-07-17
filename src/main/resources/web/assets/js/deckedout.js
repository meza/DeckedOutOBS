const createCard = (card) => {
  const enclosure = document.createElement('div');
  enclosure.classList.add('docard');
  enclosure.classList.add('slit-in-vertical');

  enclosure.style.backgroundImage = `url(/assets/cards/${card}.png)`;

  return enclosure;
};

const showCard = (card) => {
  const rootElement = document.getElementById('root');
  const cardElement = createCard(card);
  rootElement.appendChild(cardElement);

  return cardElement;
}


const getNextCard = () => {
  fetch('/nextCard')
    .then(response => response.json())
    .then(data => {
      if (data.hasCard) {
        const card = showCard(data.card);
        setTimeout(() => {
          card.classList.remove('slit-in-vertical');
          card.classList.add('slit-out-vertical');
          setTimeout(() => {
            card.remove();
            getNextCard();
          }, 1000);
        }, 9000);
      } else {

        setTimeout(() => {
          getNextCard();
        }, 1000);
      }

    })
    .catch(error => {
      console.debug('Could not get next card, sleeping for a minute', error);
      setTimeout(() => {
        getNextCard();
      }, 60000);
    })
}

getNextCard();
